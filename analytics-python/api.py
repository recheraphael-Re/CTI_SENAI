"""API interna chamada pelo Java. Processamento e gravação por lote."""
import hmac
import logging
import os
from functools import lru_cache
from typing import Annotated
from datetime import date

import pandas as pd
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field, ConfigDict
from pydantic.alias_generators import to_camel
import config
import banco
from limpeza import tratar
from analise import calcular
from main import processar_lote, DadosInvalidos

app = FastAPI(title='CTI Insights — processamento de lotes')
log = logging.getLogger(__name__)

class Linha(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
    linha: int | None = Field(default=None, ge=1)
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
    arquivoNome: str = Field(default='planilha', min_length=1, max_length=255)
    recebidas: int = Field(ge=1)
    invalidas: int = Field(default=0, ge=0)
    comAviso: int = Field(default=0, ge=0)
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
    if pedido.recebidas != len(pedido.linhas) + pedido.invalidas or pedido.comAviso > len(pedido.linhas):
        raise HTTPException(422, 'Contagens inconsistentes.')
    try:
        lote_id = processar_lote(engine(), pedido.arquivoNome,
            [l.model_dump(mode='json', exclude_none=True) for l in pedido.linhas],
            pedido.recebidas, pedido.invalidas, pedido.comAviso)
    except DadosInvalidos:
        raise HTTPException(422, 'O Python recusou os dados. Confira as regras da importação.') from None
    except Exception:
        log.exception('Falha ao processar o lote')
        raise HTTPException(503, 'Não foi possível gravar o lote. Confira banco e log do serviço.') from None
    return {'ok': True, 'loteId': lote_id, 'totalValidadas': len(pedido.linhas)}

@app.post('/testar')
def testar(linhas: list[dict]):
    """Calcula a amostra sem abrir conexão nem gravar no banco."""
    if not linhas or len(linhas) > 5000:
        raise HTTPException(422, 'Envie de 1 a 5000 linhas para o teste.')
    resultado = tratar(pd.DataFrame(linhas).astype(object))
    if resultado.tratado.empty:
        raise HTTPException(422, 'Nenhuma linha válida para analisar.')
    return calcular(resultado, len(linhas))
