# analytics-python · CTI Insights

Serviço FastAPI que padroniza os registros enviados pelo Java, calcula estatísticas e grava o lote no esquema `cti_analytics`.

## Executar

Preencha as variáveis de `.env.example` em `.env`. Use o mesmo banco e a mesma `ETL_API_KEY` do Java. Inicie o Java para preparar as tabelas ou aplique [database/schema.sql](../database/schema.sql).

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
cd ..
npm run check:etl
npm run dev:etl
```

- `GET /saude` verifica se o serviço responde.
- `POST /testar` calcula os indicadores do JSON enviado sem acessar o banco. Use `amostras/clientes.json`.
- `POST /etl` recebe as linhas do Java e confirma o lote gravado. Consulte o [contrato dos serviços](../docs/CONTRATO.md).

A gravação de clientes, contratos e estatísticas usa uma transação. Quando a gravação falha, o lote recebe status `ERRO` e as inserções parciais são revertidas.
