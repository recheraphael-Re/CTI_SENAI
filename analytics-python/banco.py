"""Banco: GRAVA no PostgreSQL (Azure) os clientes, contratos e o JSON do dashboard. As linhas chegam do backend."""
from __future__ import annotations
import json
import logging
from datetime import datetime, timezone
import pandas as pd
from sqlalchemy import create_engine, text
from sqlalchemy.engine import Connection, Engine
from config import database_url
log = logging.getLogger('analytics')

def criar_engine() -> Engine:
    return create_engine(database_url(), pool_pre_ping=True, connect_args={'connect_timeout': 10})

def agora_utc() -> datetime:
    """O Spring Boot grava TIMESTAMP em UTC (sem fuso); fazemos o mesmo."""
    return datetime.now(timezone.utc).replace(tzinfo=None)

def criar_lote(engine: Engine, arquivo_nome: str, recebidas: int, invalidas: int, validas: int, com_aviso: int) -> int:
    """Cria a linha em upload_lote com status PROCESSANDO e devolve o id novo."""
    with engine.begin() as conn:
        return conn.execute(text("INSERT INTO upload_lote (arquivo_nome, enviado_em, status, total_linhas, linhas_validas, linhas_invalidas, linhas_com_aviso, mensagem) VALUES (:a, :e, 'PROCESSANDO', :t, :v, :i, :c, 'ETL em andamento...') RETURNING id"), {'a': arquivo_nome[:255], 'e': agora_utc(), 't': recebidas, 'v': validas, 'i': invalidas, 'c': com_aviso}).scalar_one()

def marcar_erro(engine: Engine, lote_id: int, mensagem: str) -> None:
    with engine.begin() as conn:
        conn.execute(text("UPDATE upload_lote SET status = 'ERRO', mensagem = :m WHERE id = :id"), {'id': lote_id, 'm': mensagem[:480]})

def gravar_resultado(engine: Engine, lote_id: int, tratado: pd.DataFrame, payload: dict, rejeitadas_etl: int) -> None:
    """Grava tudo em uma única transação: ou o lote inteiro é processado, ou nada muda."""
    with engine.begin() as conn:
        conn.execute(text('DELETE FROM cliente WHERE lote_id = :l'), {'l': lote_id})
        conn.execute(text('DELETE FROM estatistica_lote WHERE lote_id = :l'), {'l': lote_id})
        consultores: dict[str, int] = {}
        segmentos: dict[str, int] = {}
        servicos: dict[str, int] = {}

        def id_de(tabela: str, nome: str, cache: dict[str, int]) -> int:
            if nome not in cache:
                conn.execute(text(f'INSERT INTO {tabela} (nome) VALUES (:n) ON CONFLICT (nome) DO NOTHING'), {'n': nome})
                cache[nome] = conn.execute(text(f'SELECT id FROM {tabela} WHERE nome = :n'), {'n': nome}).scalar_one()
            return cache[nome]
        for r in tratado.itertuples(index=False):
            cliente_id = conn.execute(text('\n                INSERT INTO cliente (lote_id, codigo_cti, nome, consultor_id, segmento_id, nivel, faturamento_anual, cidade, uf)\n                VALUES (:lote, :codigo, :nome, :consultor, :segmento, :nivel, :fat, :cidade, :uf)\n                RETURNING id\n            '), {'lote': lote_id, 'codigo': r.codigo_cliente[:60], 'nome': r.nome_cliente[:255], 'consultor': id_de('consultor', r.consultor[:120], consultores), 'segmento': id_de('segmento', r.segmento[:40], segmentos), 'nivel': r.nivel_cliente, 'fat': round(float(r.faturamento_anual), 2), 'cidade': r.cidade[:120] if isinstance(r.cidade, str) else None, 'uf': r.uf if isinstance(r.uf, str) else None}).scalar_one()
            for nome_servico in r.servicos:
                conn.execute(text('\n                    INSERT INTO contrato (cliente_id, servico_id, data_contratacao) VALUES (:c, :s, :d)\n                '), {'c': cliente_id, 's': id_de('servico', nome_servico[:120], servicos), 'd': r.data_contratacao.date()})
        conn.execute(text('INSERT INTO estatistica_lote (lote_id, gerado_em, resultado) VALUES (:l, :g, :r)'), {'l': lote_id, 'g': agora_utc(), 'r': json.dumps(payload, ensure_ascii=False)})
        mensagem = f'ETL concluído: {len(tratado)} clientes tratados'
        if rejeitadas_etl:
            mensagem += f', {rejeitadas_etl} linhas recusadas na limpeza'
        conn.execute(text("UPDATE upload_lote SET status = 'PROCESSADO', mensagem = :m WHERE id = :id"), {'id': lote_id, 'm': mensagem + '.'})
    log.info('Lote %s gravado: %s clientes, %s consultores, %s segmentos, %s serviços', lote_id, len(tratado), len(consultores), len(segmentos), len(servicos))
