"""Verifica os serviços ligados; --gravar autoriza criar um lote de demonstração."""
import argparse
import json
import os
import sys
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

RAIZ = Path(__file__).resolve().parents[1]

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--python', default='http://localhost:8000')
    ap.add_argument('--back', default='http://localhost:8080')
    ap.add_argument('--email', default=os.getenv('TEST_EMAIL'))
    ap.add_argument('--senha', default=os.getenv('TEST_PASSWORD'))
    ap.add_argument('--gravar', action='store_true', help='Cria um lote de teste no banco configurado')
    ap.add_argument('--exigir-todos', action='store_true', help='Retorna erro se houver qualquer teste pulado')
    op = ap.parse_args()
    totais = {'OK': 0, 'FALHA': 0, 'PULOU': 0}
    def resultado(nome, passou=None):
        estado = 'PULOU' if passou is None else 'OK' if passou else 'FALHA'
        totais[estado] += 1
        print(f'[{estado}] {nome}')
    def chamar(metodo, url, corpo=None, token=None):
        req = Request(url, method=metodo, headers={'Content-Type':'application/json'})
        if token: req.add_header('Authorization', 'Bearer ' + token)
        if corpo is not None: req.data = json.dumps(corpo).encode('utf-8')
        try:
            with urlopen(req, timeout=110) as r:
                return r.status, json.loads(r.read() or b'null')
        except HTTPError as e:
            try: return e.code, json.loads(e.read())
            except (ValueError, UnicodeError): return e.code, None
        except (URLError, TimeoutError, OSError): return None, None

    amostra = json.loads((RAIZ / 'analytics-python/amostras/clientes.json').read_text(encoding='utf-8'))
    sys.path.insert(0, str(RAIZ / 'analytics-python'))
    try:
        import pandas as pd
        from limpeza import tratar
        from analise import calcular
        dados = calcular(tratar(pd.DataFrame(amostra).astype(object)), len(amostra))
        resultado('Limpeza e média da amostra = 1.135.000', dados['resumo']['media'] == 1135000)
    except ImportError:
        resultado('Limpeza e cálculos: execute com o Python do ambiente virtual')

    st, _ = chamar('GET', op.python + '/saude')
    resultado('Python /saude', None if st is None else st == 200)
    if st == 200:
        codigo, dados = chamar('POST', op.python + '/testar', amostra)
        resultado('Python /testar sem gravação', codigo == 200 and (dados or {}).get('resumo',{}).get('media') == 1135000)
    st, _ = chamar('GET', op.back + '/api/health')
    resultado('Java /api/health', None if st is None else st == 200)
    token = None
    if st == 200:
        codigo, _ = chamar('GET', op.back + '/api/dashboard')
        resultado('Dashboard exige autenticação', codigo in [401,403])
        codigo, _ = chamar('POST', op.back + '/api/uploads/validadas', {'arquivoNome':'vazio.xlsx','linhas':[]})
        resultado('Upload exige autenticação', codigo in [401,403])
        if op.email and op.senha:
            codigo, dados = chamar('POST', op.back + '/api/auth/login', {'email':op.email,'senha':op.senha})
            token = (dados or {}).get('token') if codigo == 200 else None
            resultado('Login de conta ativa', bool(token))
        else: resultado('Login: configure TEST_EMAIL e TEST_PASSWORD')
    if token:
        codigo, dados = chamar('GET', op.back + '/api/dashboard/lotes', token=token)
        resultado('Consulta dos lotes no PostgreSQL', codigo == 200 and isinstance(dados,list))
        codigo, dados = chamar('GET', op.back + '/api/dashboard', token=token)
        resultado('Dashboard de lote existente', None if codigo == 404 else codigo == 200 and 'lote' in (dados or {}))
    if op.gravar:
        if not token: resultado('Gravação exige login e Java/Python acessíveis', False)
        else:
            boa = dict(numeroLinha=2,codigoCliente='TESTE001',nomeCliente='Cliente Teste',consultor='ana souza',
                segmento='IND.',nivelCliente='A',faturamentoAnual='1000.00',servicosContratados='Firewall',
                dataContratacao='2025-01-15',cidade='Campinas',uf='SP')
            ruim = dict(boa,numeroLinha=3,codigoCliente='TESTE002',nivelCliente='X')
            codigo,dados = chamar('POST',op.back + '/api/uploads/validadas',
                {'arquivoNome':'teste-equivalencia.xlsx','descartadas':0,'linhas':[boa,ruim]},token)
            resultado('Upload parcial: 1 válida, 1 rejeitada e lote gravado', codigo == 201 and
                (dados or {}).get('linhasValidas') == 1 and (dados or {}).get('linhasInvalidas') == 1 and
                len((dados or {}).get('rejeitadas',[])) == 1)
    else: resultado('Gravação: use --gravar para criar um lote de teste')
    print('Resumo:', ', '.join(f'{v} {k}' for k,v in totais.items()))
    return 1 if totais['FALHA'] or op.exigir_todos and totais['PULOU'] else 0

if __name__ == '__main__':
    raise SystemExit(main())
