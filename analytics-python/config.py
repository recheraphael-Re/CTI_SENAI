"""Configuração da conexão PostgreSQL usada pelo ETL."""
import os
from pathlib import Path
from dotenv import load_dotenv
from sqlalchemy.engine import URL

load_dotenv(Path(__file__).resolve().parent / '.env')

def database_url():
    """Usa os dados do Neon configurados no ambiente ou no .env local."""
    senha = os.getenv('DB_PASSWORD')
    if not senha:
        raise ValueError('Configure DB_PASSWORD no ambiente ou em analytics-python/.env.')

    return URL.create(
        'postgresql+psycopg',
        username=os.getenv('DB_USER', os.getenv('DB_USERNAME', 'postgres')),
        password=senha,
        host=os.getenv('DB_HOST', 'localhost'),
        port=int(os.getenv('DB_PORT', '5432')),
        database=os.getenv('DB_NAME', 'cti_provedor'),
        query={
            'sslmode': os.getenv('DB_SSLMODE', 'prefer'),
            'options': '-csearch_path=cti_analytics',
        },
    )
