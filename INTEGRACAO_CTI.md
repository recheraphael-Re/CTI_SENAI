# Integração CTI: Vue, Java, Python e PostgreSQL

## 1. O que foi adequado

O fluxo segue o [Projeto_Integrador_CTI](https://github.com/danielvieira95/Projeto_Integrador_CTI/tree/7931c7b829b3130387ef0abea4e9bdda6b849031):

```text
Vue lê e confere a planilha
  → Java revalida as linhas válidas
  → Python limpa, calcula e grava o lote no PostgreSQL
  → Java consulta o lote e suas estatísticas
  → Vue mostra os gráficos e o relatório salvos
```

Os módulos `limpeza.py`, `analise.py`, `insights.py`, `banco.py` e o SQL foram adaptados da referência.
As adaptações incluem os segmentos e serviços deste projeto, chave obrigatória no ETL,
retorno do identificador do lote, validação de tipos, tratamento de falhas e esquema isolado.

As tabelas JPA existentes em `public` continuam independentes dos lotes em `cti_analytics`.
Importar uma planilha não altera os cadastros dos CRUDs antigos. A autenticação existente
continua usando `usuarios`, perfis `USER`/`ADMIN` e status `ATIVO`.

## 2. Preparar o ambiente

Requisitos: Java 21, Maven, Node compatível com o Vite instalado, Python 3.11 ou superior
e PostgreSQL. As dependências Python foram verificadas com Python 3.14.
As versões diretas usadas na verificação estão em `analytics-python/requirements-verificadas.txt`.

Na raiz do projeto, em PowerShell:

```powershell
python -m venv analytics-python/.venv
python -m pip --python analytics-python/.venv/Scripts/python.exe install -r analytics-python/requirements-dev.txt
analytics-python/.venv/Scripts/python.exe analytics-python/configurar.py
```

`configurar.py` reutiliza o banco definido em `BackEnd/.env`, prepara `analytics-python/.env`
e gera uma chave `ETL_API_KEY` compartilhada quando necessário. Não conecta ao banco.
Os dois `.env` são locais e ignorados pelo Git. Se quiser configurar outro ambiente,
use os `.env.example` e mantenha host, banco, usuário, senha e chave compatíveis.
Para PostgreSQL remoto que exige TLS, configure `DB_SSLMODE=require` no Python e o
parâmetro correspondente na URL JDBC. Reinicie os serviços depois de alterar o ambiente.

## 3. Criar as tabelas de analytics

Execute uma vez no banco configurado, antes da primeira importação:

```powershell
analytics-python/.venv/Scripts/python.exe analytics-python/migrar.py
analytics-python/.venv/Scripts/python.exe analytics-python/verificar.py
```

O comando aplica `BackEnd/src/main/resources/db/analytics.sql` em uma transação.
Cria somente o esquema `cti_analytics` e suas tabelas: `upload_lote`, `consultor`,
`segmento`, `servico`, `cliente`, `contrato` e `estatistica_lote`. Não executa a migração
de usuários nem altera tabelas em `public`. O usuário do banco precisa ter permissão
para criar o esquema. A aplicação não executa essa migração automaticamente.

## 4. Iniciar os três serviços

Terminal 1, a partir da raiz:

```powershell
cd analytics-python
.venv/Scripts/python.exe -m uvicorn api:app --host 127.0.0.1 --port 8000
```

Terminal 2, a partir da raiz:

```powershell
powershell -ExecutionPolicy Bypass -File BackEnd/iniciar.ps1
```

Terminal 3, a partir da raiz:

```powershell
cd FrontEnd
npm.cmd install
npm.cmd run dev
```

Abra `http://localhost:5173` e entre com uma conta ativa. O cadastro e a aprovação de
contas continuam descritos em `BackEnd/README.md`. A API Java usa por padrão a porta
8080 e `ETL_URL=http://127.0.0.1:8000/etl`. O Vue usa `VITE_API_URL` ou localhost:8080.
O serviço Python deve ficar acessível ao backend; o navegador não precisa acessá-lo.

## 5. Importar e conferir o resultado

1. Acesse **Upload** e selecione `Data/exemplo_integracao.csv`.
2. Clique em **Ler e conferir planilha**. São 4 linhas: 3 válidas e 1 com faturamento negativo.
3. Confira as linhas e clique em **Processar e salvar no banco**.
4. O servidor deve confirmar um lote com 3 clientes. O Python cria 4 contratos, 2 consultores,
   3 segmentos e 3 serviços distintos.
5. Acesse **Gráficos**: total anual de R$ 600.000, média e mediana de R$ 200.000,
   desvio padrão amostral de R$ 100.000.
6. Em **Relatórios**, consulte os 4 contratos. O faturamento de um cliente com dois serviços
   aparece em duas linhas; não some os valores da tabela de contratos.
7. Atualize a página: os gráficos salvos são consultados no banco. Em outro acesso,
   um usuário ativo também pode consultar os lotes; a carteira é compartilhada neste projeto.

A prévia e as linhas com erro permanecem somente na sessão do navegador.
O botão de salvar é bloqueado após a confirmação de um lote. Ler novamente uma planilha
e enviá-la cria outro lote independente. Em caso de timeout, consulte o histórico antes de
reenviar: o processamento pode ter sido concluído mesmo sem o navegador receber a resposta.
O sistema não faz deduplicação automática entre envios.

## 6. Contrato da planilha

Para persistir, use as colunas:

| Coluna | Regra |
|---|---|
| `codigo_cliente` | Obrigatório, até 60 letras, números, hífens ou sublinhados; único por lote, ignorando caixa |
| `nome_cliente` | Obrigatório, até 255 caracteres |
| `consultor` | Obrigatório, até 120 caracteres |
| `segmento` | Indústria, Comércio, Serviços, Saúde, Educação, Tecnologia, Varejo, Logística, Construção, Financeiro ou Agronegócio |
| `nivel_cliente` | A, B ou C |
| `faturamento_anual` | Número finito maior ou igual a zero, dentro da capacidade NUMERIC(16,2) |
| `servicos_contratados` | Um ou mais serviços separados por `;`; nomes até 120 caracteres |
| `data_contratacao` | Data válida de 1990 a 2100; `AAAA-MM-DD` ou `DD/MM/AAAA` na planilha |
| `cidade`, `uf` | Opcionais; cidade até 120 caracteres e UF com duas letras |

As abas `dados_importacao` e `upload_clientes` são reconhecidas. A base mensal oficial
continua disponível na prévia com o gabarito original; não é convertida automaticamente
para anual. Colunas de status e valor do serviço continuam na prévia, mas não integram o
modelo de analytics da referência. Cada serviço distinto gera um contrato no lote.
As estatísticas persistidas usam desvio padrão **amostral**; a prévia antiga identifica seu
desvio como populacional. Pearson e Spearman são exploratórios; correlação não prova causa.

## 7. Rotas adicionadas e alteradas

Todas as rotas Java abaixo exigem token de usuário ativo:

| Método | Rota | Resultado |
|---|---|---|
| POST | `/api/uploads/validadas` | Revalida, processa e salva; retorna 201, `loteId`, `status`, contagens e linhas preparadas |
| GET | `/api/dashboard` | Último lote processado e estatísticas |
| GET | `/api/dashboard?loteId=1` | Estatísticas de um lote específico |
| GET | `/api/dashboard/lotes` | Últimos 50 lotes, inclusive falhas |
| GET | `/api/dashboard/lotes/1/clientes` | Clientes com uma linha por serviço contratado |

No Python: `GET /saude` confirma o processo; `POST /etl` exige `X-ETL-Key` igual a
`ETL_API_KEY`. A resposta de sucesso inclui o lote gravado. Falha de gravação desfaz clientes,
contratos e estatísticas da transação; quando o lote já existe, ele é marcado como `ERRO`.

## 8. Verificações automatizadas

Na raiz:

```powershell
cd BackEnd
mvn.cmd test
cd ../FrontEnd
node --test --test-isolation=none tests/*.test.js
npm.cmd run build
cd ../analytics-python
.venv/Scripts/python.exe -m unittest -v test_analytics
```

Os testes Java verificam autenticação, revalidação, contrato HTTP com o Python e consultas
do dashboard usando H2. Os testes Python verificam limpeza, estatísticas, autenticação do ETL,
gravação e rollback usando SQLite isolado. Os testes Vue verificam o gabarito, envio, histórico
e limpeza da sessão. Esses bancos de teste não acessam o PostgreSQL configurado.
H2 e SQLite não substituem a verificação final no PostgreSQL com os três serviços ligados.

Na verificação desta adequação, passaram 33 testes Java, 16 testes de frontend e 5 testes
Python, além do build do Vue. A conexão ao PostgreSQL definido no `.env` local não ficou
disponível; a migração não foi aplicada nesse servidor. Após restabelecer o acesso,
execute `migrar.py` e `verificar.py` e confira o exemplo com os três serviços ligados.

## 9. Erros comuns e soluções

| Sintoma | Solução |
|---|---|
| Nenhum lote aparece | Salve uma importação; apenas ler a planilha não grava dados |
| Erro ao consultar analytics | Execute a migração no mesmo banco configurado nos dois serviços |
| Falha na chamada Python | Inicie o FastAPI e confira `ETL_URL` e a chave nos dois `.env` |
| Mensagem de faturamento anual | Use coluna anual; não renomeie valores mensais sem conversão fundamentada |
| Data obrigatória | Corrija a data na planilha; linhas inválidas não são persistidas |
| Login pendente ou bloqueado | Use o fluxo de aprovação do administrador já existente |
| Timeout durante o envio | Consulte os lotes antes de tentar novamente |
| `spawn EPERM` nos testes Node | Use o comando com `--test-isolation=none` mostrado acima |

## 10. Checklist para validar no ambiente de entrega

- [ ] Os dois serviços usam o mesmo banco e a mesma chave ETL.
- [ ] A migração criou somente as tabelas de `cti_analytics`.
- [ ] Python, Java e Vue estão ligados.
- [ ] A conta usada está ativa.
- [ ] O CSV de exemplo produz 3 clientes e 4 contratos no lote.
- [ ] Gráficos e relatórios continuam disponíveis após atualizar a página.
- [ ] Desligar o Python faz o envio falhar sem apresentar sucesso.
- [ ] Documentos e gráficos adicionais exigidos pela rubrica foram conferidos.
