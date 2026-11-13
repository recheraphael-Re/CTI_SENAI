# Aula 11 - Ciência de Dados com Python

O Spring valida e envia as linhas válidas ao Python, seguindo o fluxo do
[repositório da professora](https://github.com/tatianaifsp/cti-projeto-deploy).
O POST `/etl` recebe `{arquivoNome, recebidas, invalidas, comAviso, linhas}`.
Os clientes podem usar camelCase ou snake_case.
`main.processar_lote` chama `limpeza.tratar` e `analise.calcular`, cria o lote
como `PROCESSANDO` e chama `banco.gravar_resultado`.
A gravação dos clientes, contratos e estatísticas é
transacional. O status final é `PROCESSADO` ou `ERRO`.
Dados recusados não criam lote. Falhas durante a gravação preservam o lote
com status `ERRO`, sem clientes, contratos ou estatísticas parciais.
A API retorna `{ok, loteId, totalValidadas}` para o Java confirmar o salvamento.

## Executar no PowerShell

```powershell
cd analytics-python
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python testar_conexao.py
uvicorn api:app --port 8000
```

Depois, em terminais separados, execute `mvn spring-boot:run` em `BackEnd`
e `npm run dev` em `FrontEnd`. A chave `ETL_API_KEY` deve estar configurada
igualmente nos dois serviços. GET `/saude` retorna `{"status":"ok"}`.
POST `/testar` recebe `amostras/clientes.json` e calcula sem usar o banco.
A documentação interativa fica em http://localhost:8000/docs.

## Conferência da amostra da professora

Envie `sample-data/dados.xlsx` pelo front. Esperado: 38 linhas recebidas,
32 clientes, 61 contratos, média R$ 2.261.250,00 e mediana R$ 1.470.000,00.
Confira o histórico, o dashboard e, no DBeaver, o schema `cti_analytics`:

```sql
SELECT id, status, mensagem FROM cti_analytics.upload_lote ORDER BY id DESC;
-- Substitua 1 pelo id do lote:
SELECT count(*) FROM cti_analytics.cliente WHERE lote_id = 1;
SELECT count(*) FROM cti_analytics.contrato c
JOIN cti_analytics.cliente cl ON cl.id = c.cliente_id WHERE cl.lote_id = 1;
SELECT resultado::json -> 'resumo' FROM cti_analytics.estatistica_lote WHERE lote_id = 1;
```

## Adaptações ao projeto existente

Mantivemos o schema `cti_analytics` e a relação `segmento_id`, já usados pelo Java
para ler o dashboard. `testar_conexao.py` reutiliza a verificação de conexão e tabelas.
A chamada Java é síncrona, como no GitHub: a resposta confirma o processamento.
O PDF/TXT anterior usa um fluxo diferente, em que o Spring cria o lote e envia
`loteId`. Esse contrato foi substituído pelo contrato do GitHub descrito acima.
Chaves continuam obrigatórias e dados inválidos não são gravados parcialmente.
Cada envio cria um lote independente; reenviar o mesmo arquivo cria outro lote.

## Testes locais sem Azure

```powershell
python -m unittest test_analytics.py
```

Na pasta `BackEnd`, execute `mvn test`. O teste de fluxo usa a amostra real,
a validação Vue/Java e uma instância Python com SQLite isolado.
