"""Coordena o ETL: limpar, calcular, criar o lote e gravar no PostgreSQL."""
import logging
import pandas as pd
import banco
from limpeza import tratar
from analise import calcular

log = logging.getLogger('analytics')


class DadosInvalidos(ValueError):
    """Entrada recusada antes da criação do lote."""


def processar_lote(engine, arquivo_nome: str, linhas: list[dict],
                   recebidas: int, invalidas: int, com_aviso: int = 0) -> int:
    # Confira a entrada antes de abrir um lote para evitar gravação parcial.
    if not linhas or recebidas != len(linhas) + invalidas or not 0 <= com_aviso <= len(linhas):
        raise DadosInvalidos('Contagens inconsistentes ou nenhuma linha válida.')
    res = tratar(pd.DataFrame(linhas).astype(object))
    if res.tratado.empty or not res.rejeitadas.empty:
        raise DadosInvalidos('O Python recusou linhas enviadas pelo backend.')
    payload = calcular(res, recebidas)
    lote_id = banco.criar_lote(engine, arquivo_nome, recebidas, invalidas, len(res.tratado), com_aviso)
    try:
        banco.gravar_resultado(engine, lote_id, res.tratado, payload, 0)
        return lote_id
    except Exception:
        log.exception('Falha no lote %s', lote_id)
        banco.marcar_erro(engine, lote_id, 'Falha no ETL. Consulte o log do serviço Python.')
        raise
