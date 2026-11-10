# Segunda análise de adequação e implantação

## Atualização após as correções

As divergências de código registradas abaixo foram tratadas: nível anual minúsculo,
upload parcial com rejeitadas, duplicidade sem diferenciar caixa, gráfico por consultor,
maiores clientes, data da análise, estados dos lotes, limite de 5 MB e endpoint `/testar`.
Foram acrescentados `testes/testar_tudo.py`, a amostra JSON e um teste do percurso
Vue → Java → Python com persistência isolada. A planilha do professor é verificada por
regressão sem alterar o gabarito mensal.

O empacotamento está em `compose.yaml` e `deploy/`, com instruções em [DEPLOY.md](DEPLOY.md).
Continuam dependentes do ambiente: conexão/migração no PostgreSQL real, build e execução
dos containers, domínio/HTTPS e publicação no provedor escolhido. O texto abaixo preserva
o diagnóstico anterior às correções, para consulta.

Referência consultada: [Projeto_Integrador_CTI](https://github.com/danielvieira95/Projeto_Integrador_CTI/tree/7931c7b829b3130387ef0abea4e9bdda6b849031), commit `7931c7b829b3130387ef0abea4e9bdda6b849031`.

## 1. Resultado

O fluxo principal está implementado, mas a equivalência com a referência e a prontidão
para implantação ainda têm pendências. Esta revisão executou a leitura da planilha real
`sample-data/dados.xlsx` pelo código do frontend e conferiu o tratamento no Python, sem
gravar dados. Não alterou o código da aplicação nem repetiu a conexão ao Azure.

## 2. Divergência confirmada na planilha do professor — prioridade alta

O frontend rejeita o nível `b` minúsculo na linha 35 do Excel, cliente `CTI034`, mesmo
convertendo o valor exibido para `B`. O frontend do professor aceita esse nível e o normaliza.

Origem: `FrontEnd/src/utils/planilha.js`, validação de `nivelBruto`.
A conferência usa o valor bruto contra uma lista com A/B/C, sem distinguir o contrato anual
do contrato mensal antigo. O gabarito da base mensal exige validação estrita; portanto,
a correção deve preservar essa regra para a base mensal e normalizar o nível no fluxo anual.

| Medida | Referência | Fluxo local atual |
|---|---:|---:|
| Linhas lidas | 38 | 38 |
| Linhas válidas no navegador | 32 | 31 |
| Linhas inválidas no navegador | 6 | 7 |
| Contratos calculados para as linhas enviadas | 61 | 60 |
| Média anual dos clientes válidos | R$ 2.261.250,00 | R$ 2.311.290,32 |

As contagens de contratos e médias locais foram calculadas em memória pelo Python a partir
das linhas aceitas pelo Vue; não são uma consulta ao banco. Ao receber as 38 linhas tratadas
pelo leitor, o Python sozinho reconhece 32 válidas, 61 contratos, 6 segmentos, 4 consultores,
6 serviços e média de R$ 2.261.250,00, coerente com o README do professor.

Falta um teste de regressão que execute essa planilha no frontend e confirme o resultado.
Os testes existentes do CSV pequeno e da base mensal de 300 linhas não detectaram essa diferença.

## 3. Banco real e teste completo — bloqueio de validação da implantação

A migração está em `BackEnd/src/main/resources/db/analytics.sql`, com executor
`analytics-python/migrar.py`. Na última tentativa registrada na conversa, a conexão ao Azure
expirou. Não há confirmação de que as sete tabelas de analytics foram criadas nesse servidor.

Depois de restabelecer o acesso, é necessário:

1. Conferir o banco configurado para Java e Python.
2. Aplicar a migração do esquema `cti_analytics`.
3. Iniciar Python, Java e Vue.
4. Fazer login com uma conta ativa e importar a planilha do professor.
5. Confirmar as contagens no lote e no banco, os gráficos e os relatórios após atualizar a página.

Os testes locais com H2/SQLite e o servidor HTTP simulado não substituem esse percurso.

## 4. Contrato HTTP do upload diferente da referência

Na referência, `UploadService` separa válidas e rejeitadas no servidor e devolve
`linhasValidas`, `linhasInvalidas` e `rejeitadas`. No projeto local, um item inválido faz
o envio inteiro falhar; o sucesso retorna `totalValidadas`, `descartadas`, `linhas` e `loteId`.

O frontend local usa esse contrato e consegue trabalhar com ele. Entretanto, o teste de
upload misto em `testes/testar_tudo.py --gravar` do professor não funcionará sem adaptação.
Para equivalência estrita, é necessário alinhar o tratamento parcial e a resposta nas três
camadas, ou documentar e adaptar os testes ao contrato escolhido. Não basta copiar o script.

Há também uma inconsistência de duplicidade em `UploadService`: seu conjunto usa o código
com `trim()`, mas sem normalizar a caixa. Como cada item é enviado separadamente ao
`ClienteValidacaoService`, códigos como `CTI1` e `cti1` não são detectados juntos nessa etapa.
O Python detecta a duplicidade após converter os códigos para maiúsculas e recusa o lote.
Não foi constatada persistência duplicada, mas a regra deve ser aplicada no Java ao lote inteiro.

## 5. Dashboard parcialmente equivalente

O Python já calcula `porConsultor` e `topClientes`, mas `AnalyticsPanel.vue` não os exibe.
A referência mostra gráfico de faturamento por consultor e tabela dos cinco maiores clientes.
O projeto local mostra faturamento por segmento como gráfico adicional, que pode ser mantido.

Outras diferenças:

- A referência usa desvio padrão como um dos cinco cartões. Localmente ele está na seção
  descritiva, e o quinto cartão mostra consultores. A informação existe; muda a apresentação.
- A referência devolve `geradoEm` no topo da resposta. O projeto local o mantém dentro do
  JSON das estatísticas e não o apresenta na tela.
- O dashboard do professor mostra a mensagem de erro de um lote e acompanha processamento.
  O local responde 409 quando não há estatísticas. Sua consulta de lotes não inclui `mensagem`,
  e a tela não diferencia o motivo de falha de um lote do estado de processamento.

Esses itens não impedem o servidor de iniciar, mas faltam para reproduzir toda a experiência
de consulta da referência.

## 6. Diagnóstico integrado e limite de arquivo

A referência tem `testes/testar_tudo.py`, `analytics-python/amostras/clientes.json` e
`POST /testar`, para calcular sem gravar no banco. Esses três itens não estão presentes
no projeto local. Existem testes automatizados próprios, mas falta um verificador integrado
compatível com os endpoints e credenciais reais do projeto.

O frontend do professor também limita o arquivo a 5 MB. A seleção local verifica a extensão,
mas não o tamanho. O limite Java de 5.000 linhas atua depois da leitura no navegador e não
substitui um limite do tamanho do arquivo.

## 7. Configuração necessária antes de hospedar

Os exemplos locais estão adequados ao desenvolvimento, mas a publicação precisa configurar:

| Configuração | Valor ou cuidado necessário |
|---|---|
| `VITE_API_URL` | URL pública HTTPS da API Java, sem acrescentar `/api` ao final no projeto local |
| `CORS_ALLOWED_ORIGINS` | Origem real do frontend hospedado |
| `ETL_URL` | Endereço do Python acessível ao Java; localhost só serve se estiverem no mesmo ambiente de rede |
| `ETL_API_KEY` | Mesmo segredo nos dois serviços |
| `JWT_SECRET_BASE64` | Segredo persistente configurado no serviço Java |
| Python | Comando de inicialização que escute na interface e porta exigidas pela hospedagem |
| Frontend | Publicar o build de `FrontEnd/dist` e configurar fallback para `index.html` nas rotas do Vue |
| Banco | TLS, regras de acesso e permissão de criação/leitura/escrita no esquema de analytics |
| JPA | Revisar o uso de `JPA_DDL_AUTO=update` antes de publicar; o próprio README recomenda `validate` em ambiente controlado |

Não foi encontrada configuração específica de hospedagem do frontend para o fallback.
O destino de hospedagem de Vue, Java e Python ainda precisa ser definido; a existência do
PostgreSQL no Azure, por si só, não publica os três serviços. A referência também não exige
Docker ou CI/CD para executar o projeto; a ausência dessas ferramentas não é uma falha de equivalência.

Não copie os `.env` do professor literalmente:

- Ele usa `ETL_KEY` no Java; o projeto local usa `ETL_API_KEY`.
- Ele usa `CORS_ORIGINS`; o local usa `CORS_ALLOWED_ORIGINS`.
- Ele usa `JWT_SECRET`; o local usa `JWT_SECRET_BASE64`.
- A URL-base do frontend dele termina em `/api`; a local já acrescenta os prefixos nas chamadas.

## 8. O que já está presente

- Vue, Pinia, Axios e componentes de gráficos.
- Cadastro, login JWT, aprovação/bloqueio e proteção das rotas.
- Leitura de `upload_clientes`, validação Java e integração HTTP com o Python.
- `Padronizacao.java`, limpeza e padronização no Python.
- Estatísticas, correlação exploratória e textos de insights.
- Gravação transacional de clientes, contratos e estatísticas por lote.
- Histórico de lotes, consulta de clientes/serviços e gráficos com dados do banco.
- Planilha `sample-data/dados.xlsx` e CSV de demonstração.
- Migração isolada em `cti_analytics` e instruções de execução.

Classes como `Colunas`, `Datas`, `AppProperties` ou um arquivo `main.py` não precisam ser
copiados apenas para igualar nomes: as responsabilidades correspondentes estão distribuídas
no código local. A tabela `usuarios`, o status `ATIVO` e o esquema `cti_analytics` são diferenças
de arquitetura documentadas, não funcionalidades ausentes.

## 9. Ordem de conclusão recomendada

1. Corrigir a validação anual do nível e testar a planilha do professor, preservando o gabarito mensal.
2. Alinhar os pontos de contrato do upload e a duplicidade no Java.
3. Completar a apresentação do dashboard e o diagnóstico dos lotes.
4. Acrescentar verificação integrada e limite de arquivo.
5. Validar migração e importação no Azure quando o acesso estiver disponível.
6. Configurar os três serviços no destino de hospedagem e executar o teste completo publicado.

Fontes da referência: [README](https://github.com/danielvieira95/Projeto_Integrador_CTI/blob/7931c7b829b3130387ef0abea4e9bdda6b849031/README.md),
[store de upload](https://github.com/danielvieira95/Projeto_Integrador_CTI/blob/7931c7b829b3130387ef0abea4e9bdda6b849031/frontend/src/stores/upload.js),
[UploadService](https://github.com/danielvieira95/Projeto_Integrador_CTI/blob/7931c7b829b3130387ef0abea4e9bdda6b849031/backend/src/main/java/com/cti/provedor/service/UploadService.java),
[dashboard](https://github.com/danielvieira95/Projeto_Integrador_CTI/blob/7931c7b829b3130387ef0abea4e9bdda6b849031/frontend/src/views/DashboardView.vue),
[teste integrado](https://github.com/danielvieira95/Projeto_Integrador_CTI/blob/7931c7b829b3130387ef0abea4e9bdda6b849031/testes/testar_tudo.py).
