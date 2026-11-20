# Contrato entre Vue, Spring Boot e Python

## Planilha

A leitura aceita XLSX, XLS e CSV com até 5 MB. O navegador reconhece as abas do projeto e variações de cabeçalhos. O envio ao ETL requer código, nome do cliente, consultor, segmento, nível A/B/C, faturamento anual, serviço e data de contratação. Cidade e UF são opcionais. Separe múltiplos serviços com ponto e vírgula.

A planilha de 300 registros usa faturamento mensal e pode ser revisada no navegador. Para gravar um lote no banco, use uma base que informe o faturamento anual, como `sample-data/dados.xlsx`.

## API Java

| Método e rota | Acesso e resultado |
| --- | --- |
| `GET /api/health` | Público. Confirma que a API responde |
| `POST /api/auth/register` | Público. Recebe `nome`, `email` e `senha` |
| `POST /api/auth/login` | Público. Recebe `email` e `senha`, retorna token e dados da conta ativa |
| `GET /api/auth/me` | Autenticado. Retorna a conta da sessão |
| `GET /api/admin/usuarios` | ADMIN. Lista contas com filtro opcional `status` |
| `PATCH /api/admin/usuarios/{id}/status` | ADMIN. Recebe `status` e opcionalmente `perfil` |
| `POST /api/uploads/validadas` | Autenticado. Revalida linhas, encaminha ao ETL e confirma o lote |
| `GET /api/dashboard` | Autenticado. Consulta o último lote processado ou o lote passado em `?loteId=` |
| `GET /api/dashboard/lotes` | Autenticado. Lista os lotes |
| `GET /api/dashboard/lotes/{id}/clientes` | Autenticado. Lista os contratos e os clientes do lote |

Use `Authorization: Bearer <token>` nas rotas autenticadas. Os status das contas são `PENDENTE`, `ATIVO` e `BLOQUEADO`.

## Upload e ETL

O Vue envia `arquivoNome`, `descartadas` e `linhas`. Cada registro inclui `numeroLinha`, `codigoCliente`, `nomeCliente`, `consultor`, `segmento`, `nivelCliente`, `faturamentoAnual`, `servicosContratados`, `dataContratacao`, `cidade` e `uf`.

O Spring revalida as linhas. Com pelo menos uma aprovada, encaminha `arquivoNome`, `recebidas`, `invalidas`, `comAviso` e `linhas` ao `POST /etl`, com a chave no cabeçalho `X-ETL-Key`. O Python grava o lote e retorna `ok`, `loteId` e `totalValidadas`. O Java responde HTTP 201 com o identificador confirmado, as contagens e as rejeições encontradas pelo servidor.
