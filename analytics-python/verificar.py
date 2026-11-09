"""Verifica acesso e tabelas sem gravar dados; não imprime credenciais."""
from sqlalchemy import text
from banco import criar_engine

def verificar():
    try:
        with criar_engine().connect() as conn:
            total = conn.scalar(text("SELECT count(*) FROM information_schema.tables WHERE table_schema = 'cti_analytics'"))
        print(f'Conexão PostgreSQL OK. Tabelas em cti_analytics: {total}. Esperado: 7.')
        return 0 if total == 7 else 2
    except Exception as erro:
        print('Não foi possível acessar o PostgreSQL. Confira rede, credenciais e TLS nos arquivos .env locais.')
        mensagem = str(getattr(erro, 'orig', erro)).lower()
        motivo = ('Tempo de conexão esgotado.' if 'timeout' in mensagem or 'timed out' in mensagem else
            'Nome do servidor não pôde ser resolvido.' if 'resolve' in mensagem or 'translate host' in mensagem else
            'Autenticação recusada pelo banco.' if 'password authentication' in mensagem else
            'Conexão recusada ou indisponível.' if 'refused' in mensagem else
            'Verifique a disponibilidade do servidor e as opções de conexão.')
        print(motivo)
        return 1

if __name__ == '__main__':
    raise SystemExit(verificar())
