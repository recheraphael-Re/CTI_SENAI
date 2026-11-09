"""API interna chamada pelo Java. Processamento e gravação por lote."""
import hmac
import logging
import os
from functools import lru_cache
from typing import Annotated
from datetime import date

import pandas as pd
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field
import config
import banco
from limpeza import tratar
from analise import calcular

app = FastAPI(title='CTI Insights — processamento de lotes')
log = logging.getLogger(__name__)

class Linha(BaseModel):
    linha: int = Field(ge=1)
    codigo_cliente: str = Field(min_length=1, max_length=60, pattern=r'^[A-Za-z0-9_-]+$')
    nome_cliente: str = Field(min_length=1, max_length=255)
    consultor: str = Field(min_length=1, max_length=120)
    segmento: str = Field(min_length=1, max_length=40)
    nivel_cliente: str = Field(pattern=r'^[ABC]$')
    faturamento_anual: float = Field(ge=0, le=99999999999999.99, allow_inf_nan=False)
    servicos_contratados: str = Field(min_length=1, max_length=2000)
    data_contratacao: date = Field(ge=date(1990, 1, 1), le=date(2100, 12, 31))
    cidade: str = Field(default='', max_length=120)
    uf: str = Field(default='', pattern=r'^$|^[A-Za-z]{2}$')

class Pedido(BaseModel):
    arquivoNome: str = Field(min_length=1, max_length=255)
    recebidas: int = Field(ge=1)
    invalidas: int = Field(ge=0)
    linhas: list[Linha] = Field(min_length=1, max_length=5000)

@lru_cache
def engine():
    return banco.criar_engine()

def autenticar(chave):
    esperada = os.getenv('ETL_API_KEY', '')
    if not esperada:
        raise HTTPException(503, 'Configure ETL_API_KEY no serviço Python.')
    if not hmac.compare_digest(chave or '', esperada):
        raise HTTPException(401, 'Chave do ETL inválida.')

@app.get('/saude')
def saude():
    return {'status': 'ok'}

@app.post('/etl')
def etl(pedido: Pedido, x_etl_key: Annotated[str | None, Header()] = None):
    autenticar(x_etl_key)
    if pedido.recebidas != len(pedido.linhas) + pedido.invalidas:
        raise HTTPException(422, 'Contagens inconsistentes.')
    # Nunca aceite um lote parcial silenciosamente após a revalidação no Python.
    res = tratar(pd.DataFrame([l.model_dump(mode='json') for l in pedido.linhas]).astype(object))
    if not res.rejeitadas.empty:
        raise HTTPException(422, 'O Python recusou linhas; confira os dados antes de reenviar.')
    payload = calcular(res, pedido.recebidas)
    lote_id = None
    try:
        conexao = engine()
        lote_id = banco.criar_lote(conexao, pedido.arquivoNome, pedido.recebidas,
                                    pedido.invalidas, len(res.tratado), 0)
        banco.gravar_resultado(conexao, lote_id, res.tratado, payload, 0)
        return {'ok': True, 'loteId': lote_id, 'totalValidadas': len(res.tratado)}
    except Exception:
        log.exception('Falha ao persistir lote %s', lote_id)
        if lote_id is not None:
            try:
                banco.marcar_erro(conexao, lote_id, 'Falha na gravação. Consulte o log do serviço Python.')
            except Exception:
                log.exception('Falha ao registrar erro do lote')
        raise HTTPException(503, 'Não foi possível gravar o lote. Confira banco e migração.') from None
