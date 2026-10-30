> **Acesso ao sistema:** cadastro e login utilizam o backend Spring Boot. O painel exige uma conta ativa e senha. Configure `VITE_API_URL=http://localhost:8080` em `FrontEnd/.env.local` se necessário e reinicie o Vite. Novas contas aguardam aprovação do administrador. Veja as instruções em [README do backend](../BackEnd/README.md).

﻿# CTI Insights — Projeto Integrador II 2026/02

Front-end acadêmico em Vue 3, Vite, Tailwind CSS, Pinia e Chart.js para importar e analisar a carteira de clientes da CTI.

## Executar e verificar

```bash
npm install
npm run dev
npm test
npm run build
npm run preview
```

No PowerShell com bloqueio de scripts, use `npm.cmd` no lugar de `npm`.
Os scripts do Vite usam `--configLoader native` para carregar `vite.config.js`
diretamente pelo Node, evitando a falha de empacotamento da configuração com
a dependência nativa do Tailwind no Windows. A correção foi verificada com Node 24.14.0.
Após alterar a configuração, reinicie o servidor de desenvolvimento.
O teste de regressão utiliza `../Data/CTI_Insights_Base_Validacao_300_Registros.xlsx`.
Se ela não existir no checkout, apenas esse teste é marcado como ignorado.

## Fluxo disponível

1. Em `/app/upload`, selecione ou arraste um XLSX, XLS ou CSV e processe.
2. Confira quantidades de linhas válidas, linhas com erro e ocorrências.
3. Em `/app/relatorios`, consulte as linhas, pesquise e filtre os erros.
4. Em `/app/graficos`, veja faturamento, distribuição por segmento e nível, evolução por mês de início e frequência de serviços.

### Aula 9 — Upload com Pinia

`main.js` ativa o Pinia. `stores/uploadStore.js` guarda o arquivo selecionado,
os dados originais (campos reconhecidos antes da padronização), os dados tratados,
erros e estado de carregamento. A leitura e as regras de tratamento são delegadas
a `utils/planilha.js`, preservando os testes existentes.

`UploadView.vue` chama `selecionarArquivo()` e `processarPlanilha()` e exibe
uma prévia das primeiras dez linhas, com consultor, segmento, nível e validação.
Os getters `totalClientes` e `clientesNivelA` contam somente clientes válidos;
`totalErros` conta ocorrências. Os campos internos `id`, `empresa` e `nivel`
correspondem a código do cliente, nome do cliente e nível do exemplo da aula.

O objeto File permanece em memória e deve ser selecionado novamente após F5;
o resultado importado permanece na sessão. Teste a seleção e o arrastar e soltar,
um arquivo inválido e o descarte da planilha.

O backend Spring Boot já está implementado. Configure o PostgreSQL conforme o README do backend. A publicação no Azure fica para a próxima etapa. O planejamento está em
`src/services/README_FUTURO_BACKEND.md`; a Aula 11 acrescenta o cliente Axios para autenticação.

Os filtros de segmento e nível afetam os gráficos de contagem e a análise descritiva.
Os indicadores gerais e o gráfico de faturamento mostram a base completa, conforme os rótulos.
São calculados média, mediana e desvio padrão populacional para a seleção.
Linhas inválidas não entram nos indicadores. Sem importação, o painel identifica a amostra como fictícia.

## Contrato de importação

Colunas obrigatórias: código do cliente, empresa/cliente, consultor, segmento, nível, faturamento e serviço.
Os apelidos aceitos estão em `src/utils/planilha.js`.
Colunas opcionais: valor do serviço, status e data de início.
Colunas duplicadas para o mesmo campo são rejeitadas para evitar escolha silenciosa.

A aba `dados_importacao` tem preferência. Sem ela, utiliza-se a primeira aba.
As listas da aba `listas_referencia`, quando presentes, fornecem os domínios.
O gabarito oficial é preservado: 300 linhas, 265 válidas, 35 com erro, 37 ocorrências.
Além dessas regras, a importação acusa números inválidos e campos essenciais ausentes.

O cabeçalho distingue faturamento anual, mensal ou período não informado.
Não há conversão automática entre períodos. Faixas numéricas são representadas pelo ponto médio,
uma estimativa, e não pelo faturamento exato. O faturamento das empresas clientes não representa
receita da CTI. Valores monetários na interface são abreviados e arredondados.

O gráfico temporal conta registros por mês de início, independentemente do status atual,
e exclui registros sem data. Mostra somente meses observados; não prova evolução histórica
completa nem previsão de contratações. A importação atual exige um código único por cliente
em cada arquivo. Vários contratos por cliente ainda dependem da modelagem do backend.

## Persistência e demonstrações

Tudo é processado no navegador. `sessionStorage` mantém o último envio ao atualizar a aba;
isso não substitui banco de dados e pode falhar se a capacidade do navegador for excedida.
A chave da sessão foi versionada para que dados importados sob regras antigas não escapem
das novas validações: reimporte a planilha após esta atualização.

Login e cadastro agora chamam a API; o painel exige conta ativa e a administração exige ADMIN. Sem backend, o login informa falha de conexão. Veja `src/services/CONTRATO_API.md` e `.env.example`.
O formulário de contato não envia nem armazena mensagens e informa essa limitação.

## Pendências do PI2

- Java/Spring Boot: entidades Cliente, Consultor, Contrato, Servico, Insight e Telemetria;
  CRUD, exceções customizadas, CORS, importação e integração com Python.
- PostgreSQL/Neon: esquema relacional, persistência e histórico de importações.
- Python/Pandas: tratamento, estatísticas por grupo, teste estatístico interpretado,
  gráficos de dispersão, histograma e boxplot, além do relatório de insights.
- Integração do Vue com a API; validação e autorização no servidor.
- Verificação visual em duas larguras de tela e evidências dos testes do fluxo completo.
- Publicação e documentação da arquitetura, configuração e execução integrada.
- Entrega E1: identificação, hipóteses testáveis, diagrama, protótipos, responsabilidades
  e comprovante SAGA. Documentação de Automação Industrial e Relações Humanas.

Utilizar a base fictícia para demonstrações e manter credenciais fora do repositório.

## Aula 11 — autenticação e administração

Contrato proposto, configuração e checklist: [CONTRATO_API.md](src/services/CONTRATO_API.md).
As rotas `/app/*` exigem sessão e `/app/usuarios` exige ADMIN. Após login, o painel abre em `/app/graficos`. O backend deve implementar os cinco endpoints do contrato, incluindo `/auth/me` para restaurar a sessão após F5. Não há contas de demonstração ou backend incluído.

O token permanece no sessionStorage da aba; logout remove a sessão e limpa a planilha importada. Senhas não são persistidas. O servidor deve aplicar autorização em todas as rotas protegidas.
