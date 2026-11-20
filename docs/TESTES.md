# Testes do CTI Insights

## Testes locais

Em `FrontEnd/`, instale as dependências com `npm ci`. Execute `npm test` para conferir sessão, validação de planilhas e consulta dos lotes. Execute `npm run build` para compilar o site.

Em `analytics-python/`, crie a virtualenv `.venv` e execute:

```powershell
.\.venv\Scripts\python.exe -m pip install -r requirements-dev.txt
.\.venv\Scripts\python.exe -m unittest test_analytics.py
```

Em `BackEnd/`, execute `mvn clean verify`. Os testes usam H2 para a API e SQLite para o ETL integrado. O teste integrado exige as dependências do frontend e a `.venv` do Python preparadas nos caminhos acima.

## Conferência no navegador

| Base | Resultado esperado |
| --- | --- |
| `Data/CTI_Insights_Base_Validacao_300_Registros.xlsx` | 300 linhas, 265 válidas, 35 com erro e 37 ocorrências. Compare com a aba `gabarito_erros` |
| `sample-data/dados.xlsx` | 38 linhas, 32 válidas, 6 descartadas, 61 contratos e faturamento médio anual de R$ 2.261.250,00 |

Com os serviços ativos, execute `python testes/testar_tudo.py` a partir da raiz para verificar os endpoints de saúde. Configure `TEST_EMAIL` e `TEST_PASSWORD` para também testar uma sessão autenticada. Use `--gravar` apenas se quiser inserir um lote de teste no banco configurado.
