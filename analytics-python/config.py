"""Configuração local; o esquema de analytics não altera os cadastros JPA."""
import os
from pathlib import Path
from dotenv import load_dotenv
from sqlalchemy.engine import URL

load_dotenv(Path(__file__).resolve().parent / '.env')

def database_url():
    if not os.getenv('DB_PASSWORD'):
        raise ValueError('Configure DB_PASSWORD no ambiente ou em analytics-python/.env.')
    return URL.create('postgresql+psycopg',
        username=os.getenv('DB_USER', os.getenv('DB_USERNAME', 'postgres')),
        password=os.environ['DB_PASSWORD'], host=os.getenv('DB_HOST', 'localhost'),
        port=int(os.getenv('DB_PORT', '5432')), database=os.getenv('DB_NAME', 'cti_provedor'),
        query={'sslmode': os.getenv('DB_SSLMODE', 'prefer'),
               'options': '-csearch_path=cti_analytics'})
