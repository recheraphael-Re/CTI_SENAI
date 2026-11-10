"""Testes locais: limpeza, API, persistência e rollback, sem PostgreSQL remoto."""
import json
import os
import unittest
from unittest.mock import patch
import pandas as pd
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, text, event
from sqlalchemy.pool import StaticPool
import api
import banco
from limpeza import tratar
from analise import calcular

def linha(**alteracoes):
    return dict(linha=2, codigo_cliente='CTI1', nome_cliente='Empresa', consultor='ANA SOUZA',
        segmento='IND.', nivel_cliente='A', faturamento_anual=100.0,
        servicos_contratados='Internet Dedicada; Firewall; Firewall', data_contratacao='2025-01-15',
        cidade='Campinas', uf='sp', **alteracoes)

def banco_local():
    engine = create_engine('sqlite://', poolclass=StaticPool, connect_args={'check_same_thread': False})
    @event.listens_for(engine, 'connect')
    def fks(db, _):
        db.execute('PRAGMA foreign_keys=ON')
    ddl = [
        'CREATE TABLE upload_lote (id INTEGER PRIMARY KEY, arquivo_nome TEXT, enviado_em TIMESTAMP, status TEXT, total_linhas INT, linhas_validas INT, linhas_invalidas INT, linhas_com_aviso INT, mensagem TEXT)',
        *[f'CREATE TABLE {t} (id INTEGER PRIMARY KEY, nome TEXT NOT NULL UNIQUE)' for t in ['consultor', 'segmento', 'servico']],
        'CREATE TABLE cliente (id INTEGER PRIMARY KEY, lote_id INT REFERENCES upload_lote(id), codigo_cti TEXT, nome TEXT, consultor_id INT REFERENCES consultor(id), segmento_id INT REFERENCES segmento(id), nivel TEXT, faturamento_anual NUMERIC, cidade TEXT, uf TEXT, UNIQUE(lote_id, codigo_cti))',
        'CREATE TABLE contrato (id INTEGER PRIMARY KEY, cliente_id INT REFERENCES cliente(id) ON DELETE CASCADE, servico_id INT REFERENCES servico(id), data_contratacao DATE, UNIQUE(cliente_id, servico_id))',
        'CREATE TABLE estatistica_lote (lote_id INT PRIMARY KEY REFERENCES upload_lote(id), gerado_em TIMESTAMP, resultado TEXT)'
    ]
    with engine.begin() as conn:
        for sql in ddl:
            conn.execute(text(sql))
    return engine

class AnalyticsTest(unittest.TestCase):
    def setUp(self):
        self.engine = banco_local()
        self.env = patch.dict(os.environ, {'ETL_API_KEY': 'chave-teste'})
        self.env.start()
        self.conexao = patch('api.engine', return_value=self.engine)
        self.conexao.start()
        self.client = TestClient(api.app)

    def tearDown(self):
        self.client.close(); self.conexao.stop(); self.env.stop(); self.engine.dispose()

    def enviar(self, linhas=None, chave='chave-teste', **extra):
        return self.client.post('/etl', headers={'X-ETL-Key': chave}, json={
            'arquivoNome': 'teste.xlsx', 'recebidas': 3, 'invalidas': 2,
            'linhas': linhas if linhas is not None else [linha()], **extra})

    def test_grava_lote_com_servicos_distintos_e_estatisticas(self):
        r = self.enviar()
        self.assertEqual(r.status_code, 200, r.text)
        with self.engine.connect() as conn:
            self.assertEqual(conn.scalar(text('SELECT count(*) FROM cliente')), 1)
            self.assertEqual(conn.scalar(text('SELECT count(*) FROM contrato')), 2)
            self.assertEqual(conn.scalar(text('SELECT nome FROM consultor')), 'Ana Souza')
            lote = conn.execute(text('SELECT * FROM upload_lote')).mappings().one()
            self.assertEqual((lote['status'], lote['linhas_validas'], lote['linhas_invalidas']), ('PROCESSADO', 1, 2))
            payload = json.loads(conn.scalar(text('SELECT resultado FROM estatistica_lote')))
            self.assertEqual(payload['resumo']['media'], 100)
            self.assertIsNone(payload['resumo']['desvioPadrao'])
            json.dumps(payload, allow_nan=False)

    def test_endpoint_testar_calcula_amostra_sem_abrir_banco(self):
        from pathlib import Path
        amostra = json.loads((Path(__file__).parent / 'amostras/clientes.json').read_text(encoding='utf-8'))
        with patch('api.engine') as conexao:
            r = self.client.post('/testar', json=amostra)
            self.assertEqual(r.status_code, 200, r.text)
            self.assertEqual(r.json()['resumo']['media'], 1135000)
            conexao.assert_not_called()

    def test_falha_no_meio_da_gravacao_faz_rollback(self):
        def falhar(conn, cursor, statement, parameters, context, executemany):
            if statement.startswith('INSERT INTO estatistica_lote'):
                raise ValueError('falha simulada depois dos clientes e contratos')
        event.listen(self.engine, 'before_cursor_execute', falhar)
        with self.assertLogs('api', level='ERROR'):
            self.assertEqual(self.enviar().status_code, 503)
        with self.engine.connect() as conn:
            for tabela in ['cliente', 'contrato', 'consultor', 'segmento', 'servico', 'estatistica_lote']:
                self.assertEqual(conn.scalar(text(f'SELECT count(*) FROM {tabela}')), 0)
            self.assertEqual(conn.scalar(text('SELECT status FROM upload_lote')), 'ERRO')

    def test_recusa_chave_contagens_e_linhas_invalidas_sem_gravar(self):
        self.assertEqual(self.enviar(chave='errada').status_code, 401)
        self.assertEqual(self.enviar(recebidas=4).status_code, 422)
        for mudanca in [dict(segmento='Desconhecido'), dict(faturamento_anual=-1),
                        dict(data_contratacao='2025-02-30'), dict(codigo_cliente='A B')]:
            c = linha(); c.update(mudanca)
            self.assertEqual(self.enviar([c]).status_code, 422)
        with self.engine.connect() as conn:
            self.assertEqual(conn.scalar(text('SELECT count(*) FROM upload_lote')), 0)

    def test_lotes_independentes_e_faturamento_zero(self):
        c = linha(); c.update(faturamento_anual=0, segmento='Logística')
        self.assertEqual(self.enviar([c]).status_code, 200)
        self.assertEqual(self.enviar([c]).status_code, 200)
        with self.engine.connect() as conn:
            self.assertEqual(conn.scalar(text('SELECT count(*) FROM cliente')), 2)
            self.assertEqual(conn.scalar(text('SELECT count(*) FROM consultor')), 1)

    def test_medidas_e_correlacao_sem_nan(self):
        dados = []
        for i, valor in enumerate([100, 200, 300]):
            c = linha(); c.update(codigo_cliente=f'CLI{i}', faturamento_anual=valor,
                servicos_contratados=';'.join(['Internet Dedicada', 'Firewall', 'Cloud'][:i+1]))
            dados.append(c)
        payload = calcular(tratar(pd.DataFrame(dados)), 3)
        self.assertEqual(payload['resumo']['media'], 200)
        self.assertEqual(payload['resumo']['mediana'], 200)
        self.assertEqual(payload['resumo']['desvioPadrao'], 100)
        self.assertEqual(payload['correlacao']['pearson'], 1)
        json.dumps(payload, allow_nan=False)

if __name__ == '__main__':
    unittest.main()
