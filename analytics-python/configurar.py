"""Prepara .env locais usando o banco já configurado no backend, sem conectar a ele."""
from pathlib import Path
from urllib.parse import urlsplit, parse_qs, unquote
import secrets
from dotenv import dotenv_values

def configurar():
    pasta = Path(__file__).resolve().parent
    backend = pasta.parent / 'BackEnd' / '.env'
    destino = pasta / '.env'
    if not backend.exists():
        raise SystemExit('Configure BackEnd/.env antes de executar este comando.')
    java = dotenv_values(backend)
    python = dotenv_values(destino) if destino.exists() else {}
    url = urlsplit((java.get('DB_URL') or '').removeprefix('jdbc:'))
    if url.scheme != 'postgresql' or not url.hostname or not url.path.strip('/'):
        raise SystemExit('DB_URL deve ser jdbc:postgresql://servidor:porta/banco.')
    chave_java, chave_python = java.get('ETL_API_KEY'), python.get('ETL_API_KEY')
    if chave_java and chave_python and chave_java != chave_python:
        raise SystemExit('Os arquivos .env têm chaves ETL diferentes. Alinhe os valores antes de continuar.')
    chave = chave_java or chave_python or secrets.token_urlsafe(32)
    if not chave_java:
        with backend.open('a', encoding='utf-8') as arquivo:
            arquivo.write('\nETL_API_KEY=' + chave + '\n')
    valores = dict(DB_HOST=url.hostname, DB_PORT=str(url.port or 5432), DB_NAME=unquote(url.path.strip('/')),
        DB_USER=java.get('DB_USER') or java.get('DB_USERNAME') or 'postgres',
        DB_PASSWORD=java.get('DB_PASSWORD') or '', DB_SSLMODE=parse_qs(url.query).get('sslmode', ['prefer'])[0],
        ETL_API_KEY=chave)
    # Preserve configurações explícitas já presentes. Adicione apenas valores ausentes.
    with destino.open('a', encoding='utf-8') as arquivo:
        for nome, valor in valores.items():
            if not python.get(nome):
                seguro = valor.replace('\\', '\\\\').replace("'", "\\'")
                arquivo.write(f"\n{nome}='{seguro}'\n")
    print('Configuração local preparada. Nenhuma conexão ou alteração no banco foi realizada.')

if __name__ == '__main__':
    configurar()
