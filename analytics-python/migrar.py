"""Cria somente cti_analytics no banco configurado. Execução manual e transacional."""
from pathlib import Path
from banco import criar_engine

def migrar():
    sql = (Path(__file__).resolve().parents[1] / 'BackEnd/src/main/resources/db/analytics.sql').read_text(encoding='utf-8')
    with criar_engine().begin() as conn:
        conn.exec_driver_sql(sql)
    print('Esquema cti_analytics preparado. Cadastros existentes preservados.')

if __name__ == '__main__':
    migrar()
