"""Servidor exclusivo do teste Java: SQLite em memória, sem credenciais ou banco real."""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'analytics-python'))
import api
from test_analytics import banco_local
from sqlalchemy import text
import uvicorn

conexao = banco_local()
api.engine = lambda: conexao

@api.app.get('/conferir-teste')
def conferir():
    import json
    with conexao.connect() as c:
        resultado = {t: c.scalar(text(f'SELECT count(*) FROM {t}')) for t in
            ['cliente', 'contrato', 'consultor', 'segmento', 'servico']}
        resultado['lote'] = dict(c.execute(text('SELECT total_linhas, linhas_validas, linhas_invalidas FROM upload_lote')).mappings().one())
        resultado['estatisticas'] = json.loads(c.scalar(text('SELECT resultado FROM estatistica_lote')))
    return resultado

if __name__ == '__main__':
    # A chave é somente de teste e não altera os arquivos .env.
    import os
    os.environ['ETL_API_KEY'] = 'chave-fluxo-local'
    uvicorn.run(api.app, host='127.0.0.1', port=int(sys.argv[1]), log_level='warning')
