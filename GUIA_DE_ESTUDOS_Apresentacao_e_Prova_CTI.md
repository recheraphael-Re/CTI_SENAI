---
title: Guia de estudos do CTI Insights
subtitle: Preparação para apresentação do programa e prova
author: Material de apoio ao estudante
date: 12 de novembro de 2026
lang: pt-BR
---

**Nome:** ______________________________________________

**RA:** ____________________ **Turma:** ____________________

Este guia explica o seu projeto CTI Insights a partir dos arquivos existentes no repositório. O objetivo é conseguir acompanhar o caminho dos dados, justificar as escolhas do programa e demonstrar seu funcionamento sem depender de decorar código. A referência é a versão local após a adaptação ao fluxo do GitHub da professora: o Java valida e encaminha; o Python cria o lote, calcula e grava as análises.

O programa transforma planilhas de clientes em informações para consulta. O navegador lê o arquivo e mostra problemas; o backend verifica novamente os dados; o Python produz estatísticas e insights; o PostgreSQL guarda o resultado; e o dashboard consulta as análises salvas. Login e aprovação de usuários controlam o acesso.

**Como estudar:** leia primeiro as seções 1 a 5; depois siga o fluxo de importação e as etapas de Python e banco. Use o roteiro de apresentação para ensaiar. Resolva o simulado antes de consultar o gabarito. Os trechos identificados como didáticos são simplificações, não substitutos dos arquivos do projeto.

**Escopo da verificação:** os testes locais anteriores confirmaram 37 testes Java e sete Python, com o fluxo de amostra executado em banco isolado. Isso não comprova que uma instância do Azure ou Neon esteja disponível neste momento. Este guia não contém senhas, tokens reais ou valores do seu arquivo `.env`.

# 1 Visão geral do programa

## 1.1 Problema que o projeto resolve

Uma planilha pode conter códigos repetidos, valores inválidos, nomes escritos de maneiras diferentes e datas impossíveis. Se esses registros forem somados diretamente, o dashboard poderá mostrar resultados incorretos. O CTI Insights organiza a importação, separa erros e persiste somente os dados aceitos no fluxo de processamento.

O usuário também precisa saber se está vendo apenas uma prévia no navegador ou um resultado efetivamente salvo no servidor. Seu projeto distingue essas situações na tela de gráficos. Essa diferença é central para demonstrar o programa corretamente.

**Resposta para a apresentação:** “Meu programa importa uma planilha de clientes, identifica problemas, envia as linhas válidas para processamento e apresenta estatísticas calculadas em Python a partir de dados persistidos no PostgreSQL.”

## 1.2 Caminho principal dos dados

```text
Planilha XLSX ou CSV
        |
        v
Vue e Pinia leem e validam no navegador
        |
        v
Axios envia JSON ao Spring Boot
        |
        v
Java revalida e encaminha as linhas válidas
        |
        v
FastAPI recebe o pedido no Python
        |
        v
main.py coordena limpeza, análise e gravação
        |
        v
PostgreSQL guarda lote, clientes, contratos e estatísticas
        |
        v
Spring consulta o banco e devolve JSON
        |
        v
Vue e Chart.js exibem o dashboard
```

JSON é o formato de troca entre os serviços. Não é o banco de dados nem uma biblioteca: é uma representação textual de objetos, listas, números, textos e outros valores simples.

## 1.3 As quatro partes principais

| Parte | Tecnologia | Responsabilidade no projeto |
|---|---|---|
| Interface | Vue, Pinia e Tailwind CSS | Exibir telas, reagir às ações e organizar dados do navegador |
| Backend | Java e Spring Boot | Autenticar, autorizar, revalidar, chamar Python e consultar o banco |
| Análise | Python, pandas e SciPy | Padronizar dados e calcular estatísticas e insights |
| Persistência | PostgreSQL e SQLAlchemy | Guardar resultados e permitir consultas posteriores |

# 2 Função das pastas e arquivos da raiz

## 2.1 Pastas principais

| Pasta | O que contém e para que serve |
|---|---|
| `FrontEnd/` | Aplicação Vue executada pelo navegador; telas, stores, componentes e testes de interface e lógica |
| `BackEnd/` | Aplicação Java Spring Boot; autenticação, validação, integração HTTP e consultas |
| `analytics-python/` | Serviço FastAPI e processamento estatístico, com ambiente virtual próprio |
| `sample-data/` | Amostra usada para demonstrar e conferir o fluxo; inclui `dados.xlsx` |
| `Data/` | Outros arquivos de exemplo, como CSVs; não é o banco do sistema |
| `testes/` | Ferramentas de verificação do conjunto, exportação da amostra e servidor Python isolado |
| `deploy/` | Dockerfiles e configuração de Nginx usados para empacotar e executar serviços |
| `.vscode/` | Configurações do editor que ajudam no trabalho com o projeto |
| `.github/` | Arquivos auxiliares associados ao desenvolvimento no GitHub; sua presença não garante que haja uma automação de testes |
| `node_modules/` | Dependências JavaScript instaladas; são reconstruídas por instalação, não escritas manualmente |
| `.npm-cache/` | Cache de instalação do npm; não participa da regra de negócio |
| `tmp/` | Arquivos temporários e logs de trabalho; não é o local definitivo das análises |
| `.git/` | Metadados de versionamento; guarda histórico, branches e referências do Git |

Pastas geradas também podem existir: `FrontEnd/dist/` contém o build da interface; `BackEnd/target/` contém compilação e relatórios Java; `analytics-python/.venv/` guarda o ambiente Python; `__pycache__/` guarda bytecode Python. Não confunda essas saídas com código-fonte.

## 2.2 Arquivos de configuração da raiz

`package.json` oferece atalhos para o frontend. Por exemplo, `npm run dev` na raiz delega a execução para `FrontEnd`. O `package-lock.json` registra a resolução de dependências usada pelo npm. Já `.gitignore` informa ao Git quais arquivos normalmente devem ficar fora do versionamento, como dependências e segredos locais.

`compose.yaml` descreve os serviços Docker de Python, backend e frontend. A pasta `deploy/` fornece as receitas de construção das imagens. `.env.deploy.example` é um modelo de configuração; um arquivo de exemplo não deve conter credenciais reais.

Os arquivos `INTEGRACAO_CTI.md`, `DEPLOY.md` e os guias de estudo ajudam a explicar uso e decisões. Eles documentam o sistema; a execução depende dos programas e configurações efetivos.

## 2.3 Git e GitHub

Git registra versões localmente. GitHub hospeda repositórios e permite compartilhar código. Um commit é um registro de mudanças; uma branch permite desenvolver uma linha de trabalho separada. Alterar um arquivo não equivale a fazer commit, e criar uma branch não publica nada no GitHub.

O ajuste recente foi feito na branch `ajuste-fluxo-python-professora`. Antes de apresentar ou enviar, confirme a branch e o estado real com `git status` e `git branch --show-current`.

# 3 Organização do frontend

## 3.1 Onde procurar cada responsabilidade

| Caminho em `FrontEnd/` | Função e exemplos |
|---|---|
| `src/main.js` | Cria a aplicação, instala Pinia e Router e monta o Vue em `#app` |
| `src/App.vue` | Componente raiz da aplicação |
| `src/views/` | Páginas, como `UploadView.vue`, `GraficosView.vue`, `LoginView.vue` e `RelatoriosView.vue` |
| `src/components/` | Blocos reutilizáveis, como `CartaoIndicador.vue`, `GraficoContagem.vue` e `AnalyticsPanel.vue` |
| `src/layouts/` | Estrutura comum de páginas públicas e internas, com menus e áreas de conteúdo |
| `src/stores/` | Estado compartilhado e ações de autenticação, upload e dashboard |
| `src/services/api.js` | Instância Axios, autenticação das requisições e mensagens de erro |
| `src/utils/` | Funções de leitura de planilha, formatação e cálculos locais |
| `src/router/` | Mapeamento de URLs para páginas e proteção de navegação |
| `src/composables/` | Lógica reutilizável da Composition API, como `useTheme.js` |
| `src/style.css` | Estilos globais da aplicação |
| `tests/` | Testes JavaScript para upload, autenticação, dados e dashboard |
| `database/schema.sql` | Referência SQL existente no projeto; não é um servidor de banco dentro do Vue |

## 3.2 Anatomia de um componente Vue

Um arquivo `.vue` costuma separar lógica em `<script setup>`, estrutura visual em `<template>` e estilos em `<style>`, quando necessário. O Vue conecta o estado ao template por reatividade: quando uma dependência muda, a interface que a utiliza pode ser atualizada.

Uma **view** representa uma página do Router. Um **component** representa um bloco reutilizável. Uma **prop** transmite dados do componente pai para o filho. Um evento permite que o filho comunique uma interação ao pai. Componentes não precisam consultar diretamente o banco.

Exemplo didático:

```vue
<script setup>
import { ref, computed } from 'vue'
const quantidade = ref(2)
const dobro = computed(() => quantidade.value * 2)
</script>

<template>
  <button @click="quantidade++">Adicionar</button>
  <p>Quantidade {{ quantidade }} e dobro {{ dobro }}</p>
</template>
```

No JavaScript, `ref` costuma ser acessado por `.value`. No template, o Vue faz o desembrulhamento de refs em situações usuais, como esse exemplo. Não acrescente `.value` indiscriminadamente dentro do HTML.

## 3.3 Diretivas que você precisa reconhecer

`v-if` decide se um conteúdo deve ser renderizado. `v-for` repete conteúdo para cada elemento de uma lista. `:valor` é a forma curta de `v-bind:valor`, que passa uma expressão JavaScript como atributo ou prop. `@click` é a forma curta de `v-on:click`. `v-model` sincroniza um campo de formulário com um valor reativo.

Em listas, `:key` identifica os itens para que o Vue acompanhe sua identidade. `computed` expressa um valor derivado; `watch` executa um efeito quando uma dependência observada muda. Prefira conseguir explicar essa diferença com um exemplo: o total calculado é derivação; redirecionar ao perder o token é efeito.

# 4 Pinia e actions explicados com seu código

## 4.1 O que é Pinia

Pinia é a biblioteca de gerenciamento de estado compartilhado usada pelo seu Vue. Uma store reúne dados e operações de uma área da aplicação. Assim, upload, relatório e gráficos podem acessar informações relacionadas sem copiar tudo manualmente de uma tela para outra.

Pinia não substitui o PostgreSQL. Dados de store normalmente vivem na memória do navegador; persistência exige outro mecanismo. No seu código, alguns dados são guardados em `sessionStorage`, e o resultado definitivo do ETL é salvo no banco por meio dos serviços.

Em `src/main.js`, `app.use(createPinia())` instala Pinia na aplicação. Depois, uma tela chama `useUploadStore()` ou outra função de store para obter acesso ao estado e às operações.

## 4.2 State getters e actions

| Conceito | Significado | Exemplo real |
|---|---|---|
| State | Informação que a store guarda | `clientes`, `nomeArquivo`, `carregando`, `loteSalvo` |
| Getter | Informação derivada do estado | `clientesValidos`, `faturamentoTotal`, `totalValidas` |
| Action | Operação que executa trabalho e pode alterar estado | `importarPlanilha`, `validarNoBackend`, `limpar` |

Seu projeto usa **setup stores**, definidas com `defineStore('nome', () => { ... })`. Nesse estilo, `ref` declara estado, `computed` declara getters e funções retornadas pela store funcionam como actions. Você não precisa encontrar um objeto chamado `actions` para que existam actions.

**Action**, que você chamou de “acton”, significa ação. Ela pode selecionar um arquivo, chamar uma API, atualizar o estado ou limpar a sessão. Não é uma tecnologia separada nem um método exclusivo para requisições HTTP.

## 4.3 Exemplo didático de uma store

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useExemploStore = defineStore('exemplo', () => {
  const clientes = ref([]) // state
  const total = computed(() => clientes.value.length) // getter

  function adicionar(cliente) { // action
    clientes.value.push(cliente)
  }

  return { clientes, total, adicionar }
})
```

Leitura oral: “A store guarda uma lista de clientes. O total é derivado do tamanho dessa lista. A action adicionar recebe um cliente e altera o estado. Como o total depende da lista, ele é atualizado de forma reativa.”

## 4.4 Actions reais do upload

| Action em `uploadStore.js` | O que faz |
|---|---|
| `selecionarArquivo(novo)` | Verifica seleção, extensão e limite de tamanho antes de guardar o arquivo |
| `validarArquivo()` | Confere se existe arquivo com extensão permitida |
| `processarPlanilha()` | Encaminha o arquivo selecionado para importação |
| `importarPlanilha(arquivoSelecionado)` | Chama `lerPlanilha`, atualiza clientes, erros e resumo e tenta guardar a prévia na sessão |
| `validarNoBackend()` | Seleciona válidas, monta JSON anual, chama o Spring e exige confirmação da gravação |
| `limpar()` | Reinicia o estado do upload e remove a prévia da sessão |

Os getters `linhasValidas` e `linhasComErro` separam registros pela propriedade `valida`. `totalErros` conta ocorrências de erro; isso pode ser maior do que o número de linhas inválidas, pois uma linha pode ter vários problemas.

`clientesExibidos` usa a planilha carregada ou dados de exemplo quando não há dados. Assim, mostrar um gráfico na prévia não prova que houve importação real. A indicação de exemplo existe para evitar essa confusão.

## 4.5 Stores de autenticação e dashboard

`auth.js` guarda `token` e `usuario`; deriva `logado` e `isAdmin`; e oferece `login`, `logout` e `restaurar`. O logout também limpa as stores de upload e dashboard. Isso evita deixar informações da sessão anterior na interface.

`dashboard.js` guarda a lista de lotes, o resultado consultado, clientes, carregamento e erro. Sua action `carregar(loteId)` consulta o histórico, escolhe um lote, busca as estatísticas e, quando elas existem, consulta clientes e serviços. `limpar()` remove os dados da consulta.

## 4.6 Como preservar reatividade ao usar a store

```javascript
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore.js'

const store = useUploadStore()
const { resumo, nomeArquivo } = storeToRefs(store)
// Para executar uma ação:
await store.processarPlanilha()
```

`storeToRefs` cria referências para propriedades reativas da store. Desestruturar dados diretamente pode perder a ligação reativa, dependendo do valor extraído. Actions podem ser acessadas pela store; não é necessário convertê-las com `storeToRefs`.

## 4.7 Async await e controle de respostas antigas

`async` permite que uma função trabalhe com Promises. `await` aguarda o resultado de uma Promise dentro dessa função, sem bloquear toda a interface do navegador. `try` contém a tentativa, `catch` trata falhas e `finally` faz a finalização, como desligar o indicador de carregamento.

No upload, a variável `operacao` ajuda a ignorar respostas que ficaram antigas depois de uma nova importação ou limpeza. No dashboard, `sequencia` compara a consulta atual com a que acabou de responder. Esses controles reduzem o risco de uma resposta lenta sobrescrever uma seleção mais recente.

# 5 Leitura da planilha e validação

## 5.1 Onde a planilha é lida

O arquivo é lido no navegador por funções de `src/utils/planilha.js`, com a biblioteca SheetJS, importada como `xlsx`. A action `importarPlanilha` chama `lerPlanilha` e recebe dados originais, clientes normalizados, resumo e erros.

O serviço Python principal não recebe o arquivo Excel nesse fluxo. Ele recebe uma lista JSON de clientes enviada pelo Java. Isso separa leitura da planilha e processamento estatístico.

## 5.2 Padronizar não é a mesma coisa que validar

Padronizar é transformar representações equivalentes: tirar espaços extras, ajustar maiúsculas ou converter “IND.” para “Indústria”. Validar é decidir se o dado atende a uma regra: nível entre A, B e C; código obrigatório; valor não negativo; data existente.

Um texto pode ficar padronizado e continuar inválido. Remover espaços de uma data impossível não faz essa data existir. A ordem e os resultados de cada etapa precisam ser considerados.

## 5.3 Regras e limites observados no projeto

O frontend aceita extensões XLSX, XLS e CSV e limita o tamanho a 5 MB. Reconhece aliases de cabeçalhos, procura a linha de cabeçalho nas primeiras linhas e trata datas textuais e números seriais do Excel. Quando a aba de referência existe, suas listas podem orientar os domínios da leitura.

Há dois contextos importantes: a prévia pode lidar com planilhas e períodos diferentes, mas a action de envio ao backend exige faturamento **anual**. Ela não multiplica automaticamente um valor mensal por 12. Para salvar, também exige data de contratação dentro do intervalo considerado pelo fluxo.

Duplicidade é verificada no conjunto de códigos. Uma segunda ocorrência do mesmo código pode ser recusada. Não confunda a repetição de cliente por serviço no relatório com duplicidade indevida de cliente no lote.

## 5.4 Por que validar novamente no Java

O navegador está sob controle do usuário. Alguém pode chamar a API diretamente, modificar JavaScript ou enviar um pedido sem usar a tela. Por isso, a validação do frontend melhora a experiência, mas a API precisa manter suas próprias regras.

No Java, o DTO declara restrições de campo e os serviços verificam padronização, duplicidade e consistência. O Python ainda mantém verificações para não persistir silenciosamente dados recusados. Cada camada protege uma fronteira diferente; isso não significa que todas precisam duplicar literalmente a mesma implementação.

# 6 Axios HTTP e contrato de dados

## 6.1 O que Axios faz

Axios é a biblioteca que o frontend usa para fazer requisições HTTP. Em `services/api.js`, uma instância define endereço base, timeout e cabeçalhos. O código usa essa instância para chamar o Spring.

Um interceptador de requisição anexa `Authorization: Bearer ...` nas chamadas que exigem autenticação. Um interceptador de resposta trata situações como 401. `mensagemErro` transforma falhas em mensagens apresentáveis ao usuário.

Timeout é o prazo de espera da chamada. Ele não prova que o servidor interrompeu o processamento: o servidor pode terminar depois que o cliente desistiu. Por isso, o sistema orienta consultar o histórico antes de reenviar uma operação sem confirmação.

## 6.2 Rotas principais do seu projeto

| Método e rota | Finalidade |
|---|---|
| `POST /auth/register` | Solicitar cadastro de usuário |
| `POST /auth/login` | Autenticar e receber token |
| `GET /auth/me` | Conferir o usuário da sessão |
| `POST /api/uploads/validadas` | Receber linhas do front, revalidar e processar o lote |
| `GET /api/dashboard/lotes` | Listar importações |
| `GET /api/dashboard?loteId=1` | Consultar estatísticas de um lote |
| `GET /api/dashboard/lotes/1/clientes` | Consultar clientes e seus serviços |
| `GET /saude` no Python | Conferir se a API Python está respondendo |
| `POST /testar` no Python | Calcular uma amostra sem gravar no banco |
| `POST /etl` no Python | Executar o processamento com persistência |

O Router do Vue usa outras rotas para as páginas: `/app/upload`, `/app/graficos` e `/app/relatorios`. URL de página e endpoint de API têm responsabilidades diferentes.

## 6.3 Contrato atual entre Java e Python

Exemplo didático de corpo do `POST /etl`:

```json
{
  "arquivoNome": "clientes.xlsx",
  "recebidas": 1,
  "invalidas": 0,
  "comAviso": 0,
  "linhas": [
    {
      "linha": 2,
      "codigoCliente": "CTI001",
      "nomeCliente": "Empresa Exemplo",
      "consultor": "Ana Souza",
      "segmento": "Indústria",
      "nivelCliente": "A",
      "faturamentoAnual": 1850000,
      "servicosContratados": "Internet Dedicada;Firewall",
      "dataContratacao": "2025-01-15",
      "cidade": "Campinas",
      "uf": "SP"
    }
  ]
}
```

O cabeçalho `X-ETL-Key` identifica a chave compartilhada entre os serviços. No seu projeto, a chave deve estar configurada e correta. Ela é diferente do JWT usado na autenticação do usuário.

A resposta inclui `ok`, `loteId` e `totalValidadas`. O Java confere o retorno antes de confirmar sucesso ao frontend. O Python aceita aliases camelCase e snake_case nas linhas; internamente, o processamento trabalha com nomes em snake_case.

**Contagem essencial:** `recebidas = quantidade de linhas enviadas + invalidas`. `comAviso` é uma contagem dentro das linhas enviadas e não deve ultrapassá-las. Atualmente, a chamada Java envia zero nesse campo; não afirme que todos os avisos locais são contabilizados no banco.

## 6.4 Códigos HTTP que podem cair na prova

| Código | Interpretação no contexto |
|---|---|
| 200 | Requisição concluída com sucesso, como consulta ou ETL |
| 201 | Recurso criado; o endpoint de upload confirma a importação |
| 400 | Pedido inválido; pode aparecer quando nenhuma linha foi aprovada |
| 401 | Falta de autenticação ou credencial inválida, conforme o endpoint |
| 403 | Usuário identificado sem permissão, pendente ou bloqueado |
| 404 | Recurso não encontrado, como lote inexistente |
| 409 | Conflito, como e-mail já cadastrado |
| 422 | Dados incompatíveis com o contrato ou regras de importação |
| 502 | O backend recebeu falha ou resposta inválida do serviço Python |
| 503 | Serviço indisponível, falha de comunicação ou configuração |

O código pode mudar ao atravessar camadas: uma falha Python pode ser traduzida em erro do backend para o frontend. Leia a mensagem e os logs, além do número.

# 7 Organização do backend Java

## 7.1 Camadas e responsabilidades

O código está em `BackEnd/src/main/java/com/cti/provedor/`. A aplicação inicia por `CtiProvedorApplication.java`. As pastas seguintes organizam as responsabilidades.

| Pasta | Responsabilidade | Exemplos |
|---|---|---|
| `controller/` | Receber HTTP, ler pedidos e produzir respostas | `UploadController`, `DashboardController`, `AuthController` |
| `service/` | Executar regras e coordenar operações | `UploadService`, `ClienteValidacaoService`, `PythonEtlService` |
| `dto/` | Definir dados de entrada e saída e restrições | `ClienteDTO`, `UploadDtos`, `UsuarioDto` |
| `model/` | Representar entidades e enumerações do domínio | `Usuario`, `Cliente`, `PerfilUsuario`, `StatusUsuario` |
| `repository/` | Acessar entidades persistidas com Spring Data | `UsuarioRepository`, `ClienteRepository` |
| `config/` | Configurar segurança, JWT, CORS e componentes | `SecurityConfig`, `JwtService`, `AdminInicial` |
| `src/main/resources/` | Configuração e SQL de apoio | `application.properties`, `db/analytics.sql` |
| `src/test/java/` | Testes de regras, integração e serviços | `FluxoProfessorTest`, `PythonEtlServiceTest` |

Não diga que todo acesso ao banco passa obrigatoriamente por repository: `DashboardService` usa `JdbcTemplate` para consultar as tabelas de analytics. O projeto combina JPA em áreas de cadastro e SQL em consultas de análise.

## 7.2 DTO entidade e service

DTO é o objeto de transferência de dados. `ClienteDTO` descreve campos recebidos e restrições como `@NotBlank`, `@Size`, `@Pattern` e `@PositiveOrZero`. Um DTO pode ser um `record` Java, que facilita representar dados com componentes definidos.

Entidade JPA representa um objeto persistido por mapeamento com tabela. Service implementa regra de negócio. Controller recebe a requisição. Misturar tudo em um único controller torna o código mais difícil de entender, testar e modificar.

`@Service` e `@RestController` permitem que o Spring gerencie as classes. A injeção pelo construtor entrega dependências prontas à classe. `@Value` lê valores de configuração, como o endereço do serviço Python.

## 7.3 Caminho de uma importação no Java

1. `UploadController` recebe o corpo enviado pelo frontend.
2. `UploadService.validar` usa o Validator, controla limites e identifica códigos ou números de linha repetidos.
3. `ClienteValidacaoService` prepara clientes aceitos e aplica regras de domínio.
4. O resultado separa linhas preparadas e rejeitadas e mantém as contagens.
5. Havendo linhas válidas, `PythonEtlService.enviar` monta o JSON e faz `POST /etl`.
6. O serviço confere se a resposta confirma sucesso, ID positivo e quantidade correta.
7. O controller devolve 201 com status `PROCESSADO` quando o fluxo conclui.

**Responsabilidade atual:** o Java não cria o lote da importação. O Python cria e grava o lote. Essa decisão segue o GitHub da professora e substitui o contrato anterior do PDF/TXT, no qual o Java enviava um `loteId` já existente.

## 7.4 Maven e configuração

`pom.xml` declara dependências Java, versão do projeto e plugins de construção. Maven resolve essas dependências, compila, executa testes e empacota o programa. `application.properties` configura comportamento do Spring; variáveis de ambiente podem fornecer parâmetros do banco e do ETL.

`mvn test` testa. `mvn spring-boot:run` inicia o backend. `mvn package` gera o pacote conforme a configuração. O build e os testes precisam ser executados na pasta que contém o `pom.xml`, ou com o parâmetro apropriado de caminho.

# 8 Login autorização e armazenamento da sessão

## 8.1 Autenticação e autorização

Autenticação responde “quem é o usuário?”. Autorização responde “o que esse usuário pode fazer?”. Um login correto não concede automaticamente permissão administrativa.

No seu projeto, o cadastro público define perfil `USER`. O acesso depende também do status da conta. Os valores do enum local são `PENDENTE`, `ATIVO` e `BLOQUEADO`. Portanto, use `ATIVO` ao explicar seu código, mesmo que outro material use a palavra “aprovado”.

## 8.2 Caminho do login

1. A tela chama a action `auth.login(email, senha)`.
2. Axios envia o pedido ao backend.
3. `AuthService` busca o usuário e confere a senha com BCrypt.
4. O serviço exige conta ativa antes de emitir o token JWT.
5. O frontend guarda token em memória e tenta persistir o token em `sessionStorage`.
6. Nas próximas requisições, Axios anexa o token ao cabeçalho Authorization.
7. O filtro JWT verifica a autenticação antes da rota protegida.

BCrypt produz um hash para verificação de senha; não é uma criptografia reversível para recuperar a senha original. JWT é um token assinado que representa informações de autenticação; assinatura não significa que seu conteúdo esteja criptografado.

## 8.3 O que acontece ao atualizar a página

O estado de memória do JavaScript é recriado no carregamento. A store tenta recuperar o token de `sessionStorage`, mas não confia em um perfil de usuário armazenado localmente: `restaurar()` consulta `/auth/me` para confirmar os dados do usuário.

A prévia da planilha também tenta persistir dados serializáveis na sessão. O objeto `File` fica em memória e não é reconstruído simplesmente com JSON. Datas serializadas precisam ser restauradas como datas quando o código depende de métodos de `Date`.

## 8.4 Guardas de rota CORS e segredos

O Router impede navegação comum a telas sem sessão ou sem perfil. O backend precisa continuar verificando autorização mesmo assim. Esconder um botão no Vue não protege uma API de acesso direto.

CORS controla quais origens o navegador pode usar para acessar a API em chamadas entre origens. Não autentica usuários e não substitui JWT. As configurações de origem do front e do backend devem ser compatíveis.

O `.env` guarda configuração local e pode conter credenciais. `.env.example` ensina quais variáveis preencher. Nunca apresente senhas ou tokens reais na tela do projetor, nem os inclua nos exemplos de código enviados ao professor.

# 9 Serviço Python e bibliotecas

## 9.1 Responsabilidade dos arquivos

| Arquivo em `analytics-python/` | Função |
|---|---|
| `api.py` | Rotas HTTP, autenticação interna, modelo de pedido e tradução de erros |
| `main.py` | Função `processar_lote`, que coordena as etapas do ETL |
| `limpeza.py` | Padronização, conversão de tipos, tratamento e identificação de linhas recusadas |
| `analise.py` | Estatísticas, agrupamentos, séries mensais e correlação |
| `insights.py` | Frases em português geradas por regras sobre os indicadores |
| `banco.py` | Conexão e comandos SQL para lote, clientes, contratos e JSON |
| `config.py` | Leitura de ambiente e construção segura da URL do banco |
| `migrar.py` | Execução manual do SQL que prepara o schema `cti_analytics` |
| `verificar.py` | Verificação de acesso e de quantidade de tabelas do schema |
| `testar_conexao.py` | Entrada de teste de conexão que reutiliza `verificar` |
| `configurar.py` | Utilitário auxiliar de configuração local; confira suas instruções antes de executá-lo |
| `amostras/clientes.json` | Dados para testar cálculo sem Java ou banco |
| `test_analytics.py` | Testes locais de cálculo, API, persistência e rollback |

## 9.2 Bibliotecas e ambiente virtual

| Biblioteca | Papel efetivo |
|---|---|
| pandas | DataFrames, limpeza, agrupamentos e medidas descritivas |
| NumPy | Base de operações numéricas usada pelo ecossistema científico |
| SciPy | Testes e coeficientes estatísticos, como Pearson e Spearman |
| FastAPI | Publicação de endpoints HTTP |
| Pydantic | Validação e conversão dos modelos de entrada da API |
| Uvicorn | Servidor ASGI que executa a aplicação FastAPI |
| SQLAlchemy | Engine, transações e execução de SQL parametrizado |
| psycopg | Driver que conversa com PostgreSQL |
| python-dotenv | Carregamento de variáveis do arquivo `.env` |
| httpx | Apoio aos testes HTTP locais com o cliente de testes |

`.venv` isola os pacotes do projeto. Não é obrigatório ativá-lo se você chamar explicitamente seu executável, como `.venv\Scripts\python.exe`. Usar um Python global diferente pode causar o erro “módulo não encontrado”, mesmo com o pacote instalado em outro ambiente.

`requirements.txt` lista dependências e limites mínimos. `requirements-dev.txt` inclui essa lista e acrescenta ferramentas de teste. `requirements-verificadas.txt` registra versões fixadas no projeto. `>=` aceita versões superiores compatíveis com a restrição; `==` exige aquela versão. Fixar versões ajuda a reproduzir um ambiente, mas não é garantia de ausência de bugs.

## 9.3 O que é um DataFrame

Um DataFrame é uma estrutura tabular com linhas e colunas. Ele permite trabalhar com uma coluna inteira e agrupar por categorias. Uma Series representa uma sequência de valores, como a coluna `faturamento_anual`.

Exemplo didático:

```python
import pandas as pd

df = pd.DataFrame({
    'segmento': ['Comércio', 'Indústria', 'Comércio'],
    'faturamento_anual': [100.0, 200.0, 300.0],
})

print(df['faturamento_anual'].mean())
print(df.groupby('segmento')['faturamento_anual'].sum())
```

O primeiro resultado é `200.0`. As somas são Comércio = `400.0` e Indústria = `200.0`. O agrupamento resume os registros por categoria; não cria clientes novos.

# 10 ETL e processamento de um lote

## 10.1 Extract Transform Load

**Extract** é receber dados da origem: neste fluxo, a API Python recebe JSON do backend. **Transform** é padronizar, converter e produzir informação: `limpeza.py`, `analise.py` e `insights.py`. **Load** é persistir o resultado: `banco.py` grava no PostgreSQL.

O arquivo `main.py` coordena essas responsabilidades sem concentrar todas as regras em uma única função enorme. A separação permite testar cálculo sem abrir conexão e testar gravação sem depender de uma planilha real.

## 10.2 Ordem atual no seu programa

1. `api.py` valida formato do pedido, chave e contagens.
2. `main.processar_lote` confere a entrada e chama `tratar`.
3. Havendo linha recusada, a operação é rejeitada antes da criação de lote.
4. `calcular` produz o objeto de estatísticas e insights.
5. `banco.criar_lote` cria o registro com status `PROCESSANDO`.
6. `banco.gravar_resultado` grava clientes, contratos e estatísticas em transação e marca `PROCESSADO`.
7. Se a persistência falhar, o lote é marcado como `ERRO` e a API responde falha.

Essa ordem preserva uma melhoria local: entrada recusada não cria lote vazio. O fluxo mantém a responsabilidade de criação no Python, como no GitHub, sem exigir que cada detalhe seja cópia literal do exemplo.

## 10.3 Estados e reenvio

`PROCESSANDO` significa que o lote foi criado e ainda não concluiu a gravação. `PROCESSADO` significa conclusão bem-sucedida. `ERRO` registra falha após criação. Nem toda falha deixa lote: chave errada, pedido malformado ou dados recusados podem falhar antes do INSERT.

Cada envio aceito cria outro lote. O projeto não implementa, nesse contrato, uma chave de idempotência que identifique dois pedidos como o mesmo envio. Bloquear botão repetido na interface ajuda, mas não garante deduplicação de requisições em todos os cenários. Após timeout, consulte o histórico antes de tentar novamente.

## 10.4 Transformações que você deve explicar

`tratar` converte nomes de colunas para o formato interno e constrói um `ResultadoTransformacao`. Esse resultado contém a tabela tratada, linhas rejeitadas, contagens de padronizações e avisos. Texto, número e data precisam ter os tipos corretos antes dos cálculos.

Os serviços de um cliente são separados em lista e podem ser expandidos com `explode`. Assim, um cliente com dois serviços participa da contagem de dois serviços, mas continua sendo um cliente no total da carteira.

Na análise mensal, o código inclui meses intermediários sem contratação, preenchendo com zero. Isso é diferente de exibir apenas os meses que apareceram nos registros.

# 11 Estatística para defender o dashboard

## 11.1 Média mediana e moda

Média é a soma dos valores dividida pela quantidade. Mediana é o valor central após ordenar a lista; com quantidade par, considera-se a média dos dois centrais. Moda é o valor ou categoria mais frequente.

Exemplo: para `100, 200, 300`, média = `200` e mediana = `200`. Para `100, 200, 3000`, média = `1100` e mediana = `200`. Um valor alto pode elevar bastante a média. A mediana ajuda a observar o centro, mas nenhuma medida isolada descreve toda a distribuição.

No seu código, `moda` usa `Series.mode()` e retorna o primeiro resultado quando existe. Se houver empate, pode existir mais de uma moda; a função não devolve todas. O “serviço mais contratado” da análise de serviços considera a contagem de clientes associada a cada serviço.

## 11.2 Desvio padrão e quartis

Desvio padrão mede dispersão: quanto os valores variam em torno da média. Em `analise.py`, o desvio usa `ddof=1`, portanto é **amostral**. Com um único cliente, o resultado é `None`, enviado como `null`, pois o desvio amostral não pode ser calculado dessa forma com apenas uma observação.

Para `100, 200, 300`, o desvio padrão amostral é `100`. O desvio populacional seria diferente. Sua prévia local usa cálculo populacional em parte da interface; ao apresentar o dashboard salvo, identifique o valor como amostral.

Q1 e Q3 são quartis associados aos percentis 25 e 75. A interpolação pode produzir valores que não aparecem literalmente na lista. O intervalo entre Q1 e Q3 ajuda a observar a dispersão central. Não diga que quartil é a quarta parte do faturamento total: ele se refere à posição dos valores ordenados.

## 11.3 Agrupamentos e percentuais

Os agrupamentos por segmento, nível e consultor calculam contagens e medidas para cada categoria. Participação de clientes e participação no faturamento respondem a perguntas diferentes. Um grupo pode ter poucos clientes e grande parcela da soma dos faturamentos.

Se um cliente contrata mais de um serviço, os percentuais de presença dos serviços podem somar mais de 100%. Isso é esperado para categorias que se sobrepõem. Já os níveis de cliente, quando cada cliente tem exatamente um nível, dividem a base em grupos exclusivos.

## 11.4 Correlação de Pearson e Spearman

O programa relaciona quantidade de serviços e faturamento anual. Pearson mede associação linear; Spearman considera a ordenação dos valores. Coeficientes próximos de 1 indicam associação positiva forte; próximos de -1, associação negativa forte; próximos de zero, pouca associação do tipo considerado.

O código exige pelo menos três registros e variação nos dois campos para calcular. Quando não há condições, retorna valores nulos. Um campo constante não permite calcular esse coeficiente de maneira informativa.

O valor-p é usado no teste de correlação sob suas premissas. Ele não é a probabilidade de a hipótese nula ser verdadeira, nem prova causalidade. A análise descreve o lote; não autoriza afirmar automaticamente que contratar mais serviços causa maior faturamento.

## 11.5 Insights não são inteligência artificial generativa

`insights.py` aplica condições e monta frases a partir dos resultados. Por exemplo, identifica o segmento com mais clientes e a carteira de consultor com maior soma de faturamento. São regras programadas e texto formatado; o código não treina um modelo preditivo nem chama um modelo de linguagem nesse fluxo.

**Cuidado de interpretação:** faturamento anual é o faturamento das empresas clientes. Não representa receita da CTI, comissão do consultor nem valor dos contratos de telecomunicações.

## 11.6 Números de referência da amostra

| Indicador de `sample-data/dados.xlsx` | Resultado esperado |
|---|---:|
| Linhas recebidas | 38 |
| Linhas válidas | 32 |
| Linhas inválidas | 6 |
| Clientes gravados | 32 |
| Contratos de serviços | 61 |
| Segmentos | 6 |
| Consultores | 4 |
| Serviços distintos | 6 |
| Média do faturamento anual | R$ 2.261.250,00 |
| Mediana do faturamento anual | R$ 1.470.000,00 |

Esses valores são da amostra específica. Não use o mesmo gabarito para outra planilha, para filtros diferentes ou para a base de 300 registros mencionada em outros materiais.

# 12 Banco de dados e transações

## 12.1 PostgreSQL Neon Azure e schema

PostgreSQL é o sistema de banco. Neon e Azure são opções de hospedagem. SQLAlchemy e psycopg são bibliotecas de acesso; não substituem o servidor. Um schema é um agrupamento de objetos dentro de um banco.

Seu fluxo de analytics usa o schema `cti_analytics`. `config.py` define o `search_path` da conexão Python para que comandos como `INSERT INTO cliente` atinjam a tabela desse schema. O Java usa nomes qualificados, como `cti_analytics.cliente`, nas consultas de análise.

O sistema também contém entidades e tabelas de cadastro gerenciadas pelo backend. Não presuma que todo `Cliente` JPA representa diretamente a mesma tabela do lote de analytics. Verifique o schema e o caminho de código antes de relacionar dados.

## 12.2 Tabelas do processamento

| Tabela em `cti_analytics` | O que guarda |
|---|---|
| `upload_lote` | Nome do arquivo, data, status, contagens e mensagem de cada envio |
| `consultor` | Nomes de consultores sem repetição |
| `segmento` | Segmentos padronizados sem repetição |
| `servico` | Catálogo de serviços sem repetição |
| `cliente` | Cliente do lote com código, nome, nível, faturamento e referências |
| `contrato` | Associação cliente e serviço com data de contratação |
| `estatistica_lote` | JSON das análises e data de geração de cada lote |

## 12.3 Chaves e relacionamentos

Chave primária identifica um registro. Chave estrangeira aponta para registro de outra tabela. `cliente.lote_id` aponta para `upload_lote.id`. `cliente.consultor_id` e `cliente.segmento_id` apontam para seus cadastros. `contrato` aponta para cliente e serviço.

A restrição `UNIQUE(lote_id, codigo_cti)` impede código duplicado dentro do mesmo lote, mas permite o mesmo código em outro lote. `UNIQUE(cliente_id, servico_id)` evita repetir o mesmo serviço para o mesmo registro de cliente.

Guardar consultores, segmentos e serviços em tabelas próprias reduz repetição e facilita consistência. Isso é parte da organização relacional dos dados. A tabela `contrato` permite que um cliente tenha vários serviços.

## 12.4 Por que existem mais contratos do que clientes

Uma empresa pode contratar Internet Dedicada e Firewall. Isso gera um cliente e duas associações em `contrato`. A consulta de relatório que faz JOIN com serviços pode repetir o faturamento anual da empresa em mais de uma linha.

Não some esse faturamento por linha de contrato para descobrir o total da carteira: isso duplicaria valores. Some na tabela de clientes ou use as estatísticas já calculadas. Esse é um ótimo exemplo de erro de análise que um JOIN pode introduzir se for interpretado sem cuidado.

## 12.5 Transação commit e rollback

Uma transação reúne comandos que devem ter sucesso em conjunto. No `with engine.begin()`, SQLAlchemy confirma a transação ao sair normalmente e desfaz alterações se ocorrer exceção.

`gravar_resultado` grava clientes, contratos, estatística e status final dentro de uma transação. Se falhar depois de inserir clientes, o rollback impede que eles permaneçam sem o restante do processamento.

A criação do lote acontece antes, em transação própria. Por isso, após falha de persistência, o lote pode continuar no histórico como `ERRO`, mesmo sem clientes ou estatísticas parciais. Não diga que “qualquer erro apaga tudo do banco”: a finalidade é preservar um registro do problema sem dados de análise incompletos.

## 12.6 SQL parametrizado e migração

Parâmetros como `:id` e `:nome` separam valores de instruções SQL. Concatenar texto recebido do usuário diretamente em SQL pode permitir SQL injection. Os nomes de tabela usados internamente no código precisam continuar controlados pelo próprio programa.

`migrar.py` lê `BackEnd/src/main/resources/db/analytics.sql` e executa a preparação manual de `cti_analytics`. Não execute migração em outro banco por engano. Essa etapa usa a configuração local de conexão e tem efeito real; ela não é equivalente a rodar um teste com SQLite.

# 13 Dashboard relatórios e gráficos

## 13.1 De onde vêm os números salvos

O Python monta o objeto de estatísticas e o armazena como JSON em `estatistica_lote.resultado`. `DashboardService` consulta essa linha e devolve um objeto com lote, data de geração e estatísticas. A store `dashboard` recebe a resposta e a interface usa as listas para montar gráficos e cartões.

No seu projeto, a tela é `GraficosView.vue`, em `/app/graficos`. A área de análises salvas é `AnalyticsPanel.vue`. Não procure obrigatoriamente `DashboardView.vue` ou `BaseChart.vue`: esses são nomes presentes no exemplo da professora, enquanto seu projeto usa outros componentes.

## 13.2 Mapeamento entre resultado e tela

| Campo do JSON | Uso na interface |
|---|---|
| `resumo` | Quantidade de clientes, total, média, mediana e desvio |
| `porSegmento` | Clientes e faturamento por segmento |
| `porNivel` | Distribuição dos clientes por nível |
| `porConsultor` | Soma de faturamento das empresas por consultor |
| `porServico` | Presença dos serviços na carteira |
| `porMes` | Clientes por mês de contratação |
| `topClientes` | Tabela dos cinco maiores faturamentos |
| `correlacao` | Coeficientes, valor-p e interpretação |
| `qualidade` | Linhas recebidas, válidas e avisos de tratamento |
| `insights` | Lista de frases analíticas |

## 13.3 Chart.js e transformação dos dados

Chart.js desenha os gráficos; Vue fornece as propriedades reativas. Uma lista como `[{segmento: 'Comércio', clientes: 9}]` é transformada em rótulos e números. `map` extrai um campo de cada objeto sem alterar a lista original.

```javascript
const rotulos = itens.map(item => item.segmento)
const valores = itens.map(item => item.clientes)
```

`filter` seleciona itens por condição. `reduce` acumula um resultado, como a soma de faturamentos. `sort` ordena e altera a lista sobre a qual foi chamado; quando é preciso preservar o original, faça cópia antes.

## 13.4 Prévia local e histórico

A prévia local usa dados do navegador e utilitários JavaScript. A análise salva usa resultados Python consultados do banco. Diferenças de base, filtros e convenção estatística podem produzir números diferentes; primeiro confira se está comparando o mesmo lote e a mesma medida.

O seletor de lote permite consultar importações independentes. Ele não soma automaticamente todos os arquivos importados. O painel pode consultar novamente um lote que esteja em `PROCESSANDO`, mas a chamada de upload atual espera a conclusão de modo síncrono.

# 14 Executar e reproduzir os resultados

## 14.1 Conferir ferramentas

Execute no PowerShell:

```powershell
python --version
java -version
mvn -version
node --version
npm --version
```

São esperados números de versão, e não mensagens de comando inexistente. O Java do projeto é 21. Para compatibilidade do frontend, confira também a versão de Node exigida pelo Vite instalado. Não altere versões antes da apresentação sem testar o conjunto.

## 14.2 Preparar Python a partir da raiz

```powershell
cd C:\Users\Instrutor\CTI_SENAI
cd analytics-python

# Execute somente se o ambiente virtual ainda não existir:
python -m venv .venv

# Usa o Python do projeto sem depender da ativação:
.\.venv\Scripts\python.exe -m pip install -r requirements-dev.txt
```

Saída esperada: pacotes instalados ou mensagens de que já estão satisfeitos. O aviso de nova versão do pip não significa que a instalação falhou. O erro de arquivo não encontrado normalmente exige conferir a pasta atual.

## 14.3 Conferir o banco e iniciar Python

Preencha a configuração local com os dados autorizados do banco e a chave do ETL. Não copie `.env.example` por cima de um `.env` já configurado sem conferir o que será substituído.

```powershell
# Dentro de analytics-python:
.\.venv\Scripts\python.exe testar_conexao.py

# Somente se for necessário preparar o schema e no banco correto:
.\.venv\Scripts\python.exe migrar.py

.\.venv\Scripts\python.exe -m uvicorn api:app --port 8000
```

`testar_conexao.py` deve informar conexão PostgreSQL OK e sete tabelas em `cti_analytics`, quando a estrutura está preparada. A migração informa que o schema foi preparado. O Uvicorn deve indicar que está executando em `http://127.0.0.1:8000`.

## 14.4 Iniciar Java e frontend em outros terminais

```powershell
# Terminal do backend:
cd C:\Users\Instrutor\CTI_SENAI\BackEnd
mvn spring-boot:run
```

```powershell
# Terminal do frontend:
cd C:\Users\Instrutor\CTI_SENAI\FrontEnd
npm install
npm run dev
```

Instalação não precisa ser repetida em todo início quando as dependências já estão preparadas. O Spring deve indicar início da aplicação; o Vite informa o endereço local, normalmente `http://localhost:5173`. A porta efetiva pode variar se estiver ocupada ou configurada de outro modo.

## 14.5 Testar Python sem banco

Com a API ligada, em outro PowerShell:

```powershell
cd C:\Users\Instrutor\CTI_SENAI\analytics-python
Invoke-RestMethod http://localhost:8000/saude

$jsonAmostra = Get-Content .\amostras\clientes.json -Raw -Encoding UTF8
$resultado = Invoke-RestMethod -Method Post `
  -Uri http://localhost:8000/testar `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($jsonAmostra))
$resultado.resumo | Format-List
```

Saúde retorna status `ok`. Para os dois clientes da amostra JSON, a média esperada é `1135000`. Esse resultado não é a média da planilha de 38 linhas. A rota `/testar` não grava no banco.

## 14.6 Rodar testes locais

```powershell
cd C:\Users\Instrutor\CTI_SENAI\analytics-python
.\.venv\Scripts\python.exe -m unittest test_analytics.py
```

```powershell
cd C:\Users\Instrutor\CTI_SENAI\BackEnd
mvn test
```

```powershell
cd C:\Users\Instrutor\CTI_SENAI\FrontEnd
npm test
```

Python deve terminar com `OK`; Maven deve indicar testes aprovados e construção bem-sucedida; o teste JavaScript mostra resumo de casos aprovados. A quantidade depende da versão dos arquivos. O teste de fluxo Java depende de ferramentas e amostras locais; confira se foi executado ou pulado, não apenas se o comando terminou.

## 14.7 Conferir persistência no DBeaver

```sql
SELECT id, arquivo_nome, status, total_linhas,
       linhas_validas, linhas_invalidas, mensagem
FROM cti_analytics.upload_lote
ORDER BY id DESC;
```

Depois de identificar o ID real, substitua o `1` nas consultas:

```sql
SELECT COUNT(*) AS clientes
FROM cti_analytics.cliente
WHERE lote_id = 1;

SELECT COUNT(*) AS contratos
FROM cti_analytics.contrato c
JOIN cti_analytics.cliente cl ON cl.id = c.cliente_id
WHERE cl.lote_id = 1;

SELECT resultado::json -> 'resumo' ->> 'media' AS media
FROM cti_analytics.estatistica_lote
WHERE lote_id = 1;
```

Para a planilha de exemplo: status `PROCESSADO`, 32 clientes, 61 contratos e média `2261250`. Se estiver olhando outro lote, os resultados podem mudar.

# 15 Erros comuns e soluções

| Sintoma | Causa provável | Como investigar ou corrigir |
|---|---|---|
| `requirements-dev.txt` não encontrado | Terminal está na raiz | Entre em `analytics-python` ou informe o caminho completo |
| Pacote não encontrado no Python | Interpretador diferente da `.venv` | Use explicitamente `.venv\Scripts\python.exe` |
| Ativação PowerShell bloqueada | Política da sessão | Use o executável da `.venv` diretamente |
| Uvicorn não encontra `api` | Pasta de execução incorreta | Execute dentro de `analytics-python` |
| POST do upload falha com Python desligado | Backend não consegue chamar `/etl` | Ligue Python e confira `ETL_URL` |
| Chave interna recusada | Chaves ausentes ou diferentes | Compare a configuração local, sem divulgar valores |
| Banco inacessível | Rede, endereço, credencial ou TLS | Execute `testar_conexao.py` e confira configuração e logs |
| Tabela de analytics não existe | Schema ainda não preparado | Confira conexão e a necessidade de executar `migrar.py` |
| Usuário cadastra mas não entra | Status pendente ou bloqueado | Verifique a liberação pelo administrador |
| Erro 401 após período de uso | Token ausente, inválido ou expirado | Entre novamente e confira a chamada no Network |
| Erro de CORS | Origem da interface não autorizada | Confira as origens permitidas no backend |
| Faturamento mensal recusado no envio | ETL persistido exige anual | Use a coluna anual correta; não converta sem critério |
| Gráfico aparece sem importação | Tela está usando exemplo ou prévia | Confira a indicação de origem e o lote salvo |
| Média divergente entre telas | Base, filtro ou medida diferente | Compare mesmo lote, período e convenção |
| Total inflado no relatório | Faturamento somado após JOIN de serviços | Some clientes sem multiplicar por contrato |
| Reenvio cria nova importação | Cada pedido cria lote próprio | Consulte histórico antes de repetir envio |
| “Porta em uso” | Outro processo já escuta nela | Identifique o serviço existente antes de encerrar ou mudar porta |

## 15.1 Como localizar a camada do problema

Se a planilha nem abre, comece no leitor do frontend e no Console. Se abre, mas o envio falha, confira o pedido no Network, o status HTTP e o log Java. Se o Java conseguiu chamar Python e o processamento falhou, leia o log Python e confira banco e schema. Se há estatística no banco e gráfico vazio, investigue o contrato JSON, a consulta e o componente visual.

Essa sequência é melhor do que alterar arquivos aleatoriamente. Primeiro descubra até onde o dado chegou; depois confira a próxima fronteira.

# 16 Roteiro para apresentar o programa

## 16.1 Demonstração de aproximadamente oito minutos

| Tempo | O que demonstrar | Ideia principal |
|---|---|---|
| 0 a 1 min | Objetivo e arquitetura | Planilha vira informação confiável com rastreio por lote |
| 1 a 2 min | Login e controle de acesso | Identidade, perfil e status são verificados |
| 2 a 3 min | Importar a amostra | A prévia mostra erros antes de enviar |
| 3 a 4 min | Store de upload | State guarda dados, getters resumem e actions coordenam |
| 4 a 5 min | Processar e salvar | Java revalida e Python cria e grava o lote |
| 5 a 6 min | Dashboard e histórico | Números vêm das estatísticas persistidas |
| 6 a 7 min | Conferência no banco e testes | Mesmos valores podem ser conferidos fora da interface |
| 7 a 8 min | Limitações e conclusão | Interpretação estatística exige cuidado e testes têm escopo |

## 16.2 Fala inicial sugerida

“O CTI Insights foi desenvolvido para importar dados de clientes e apresentar análises. O frontend usa Vue e Pinia para ler a planilha, mostrar os erros e organizar o estado. O Spring Boot autentica os usuários e revalida as linhas enviadas. O Python recebe JSON, faz o tratamento com pandas, calcula os indicadores e grava o resultado no PostgreSQL. Depois, o dashboard consulta esse resultado por meio do backend.”

## 16.3 Arquivos para abrir na apresentação

Abra previamente `FrontEnd/src/stores/uploadStore.js`, `FrontEnd/src/services/api.js`, `BackEnd/src/main/java/com/cti/provedor/service/PythonEtlService.java`, `analytics-python/main.py` e `analytics-python/analise.py`. Deixe `AnalyticsPanel.vue` e o SQL disponíveis para perguntas.

Mostre um trecho de cada arquivo relacionado a uma etapa, em vez de percorrer centenas de linhas. Ao apontar uma função, explique sua entrada, processamento e saída. Exemplo: `calcular` recebe dados tratados e total recebido, monta medidas e devolve um dicionário serializável em JSON.

## 16.4 Perguntas que a professora pode fazer

**Onde está a action do botão de salvar?** Na store de upload, `validarNoBackend`, que prepara o JSON, faz a chamada e atualiza estado de sucesso ou erro.

**Quem lê o Excel?** O frontend, pelo leitor de planilha. Python recebe JSON neste fluxo.

**Quem cria o lote?** O Python, por `banco.criar_lote`, coordenado por `main.processar_lote`.

**Por que Java e Python separados?** Java mantém API, segurança e regras de entrada; Python concentra tratamento e análise. HTTP permite que processos de linguagens diferentes troquem dados.

**Se o Python falhar, o gráfico pode usar a prévia?** A prévia é uma área separada. Não deve ser apresentada como resultado persistido. O histórico e o dashboard salvo precisam refletir o status do lote.

**Como prova que salvou?** Mostro ID e status, consulto tabelas do mesmo lote e comparo a média do banco com a interface.

**O programa usa IA?** Os insights atuais são regras programadas sobre estatísticas, sem modelo generativo ou treinamento de aprendizado de máquina nesse fluxo.

## 16.5 Se algo falhar durante a apresentação

Explique o sintoma e a camada que vai conferir. Verifique a saúde do Python e o backend. Se houver lote processado anterior, use o histórico para demonstrar dados persistidos e informe que está usando uma execução anterior. Não diga que acabou de salvar quando não existe confirmação.

Tenha a planilha, os comandos e os resultados esperados acessíveis. Uma boa apresentação também mostra que você entende como investigar falhas.

# 17 Simulado para a prova

## 17.1 Questões conceituais

1. Qual problema Pinia resolve e por que ele não substitui o PostgreSQL?
2. Em setup stores, como state, getters e actions aparecem no código?
3. Qual a diferença entre `computed` e `watch`?
4. O que faz `storeToRefs` e por que ele aparece nas telas?
5. Qual action lê a planilha e qual envia os dados para salvar?
6. Por que o backend valida informações já validadas pelo frontend?
7. Qual a diferença entre DTO, entidade e service?
8. O que Axios faz e qual a função dos interceptadores?
9. Qual a diferença entre JWT e `X-ETL-Key`?
10. Qual camada cria o lote no contrato atual?
11. Explique Extract, Transform e Load com nomes de arquivos.
12. Qual a diferença entre FastAPI e Uvicorn?
13. O que são DataFrame e Series?
14. Por que a quantidade de contratos pode superar a de clientes?
15. Explique commit e rollback no processamento.
16. Por que um lote pode ficar em `ERRO` sem clientes gravados?
17. Compare média, mediana e moda.
18. Qual desvio padrão aparece no resultado Python salvo?
19. Correlação positiva prova que contratar serviços aumenta faturamento?
20. Quais cuidados são necessários ao somar dados após um JOIN?

## 17.2 Exercícios de raciocínio

21. Um pedido tem 38 recebidas, 32 linhas enviadas e seis inválidas. As contagens estão consistentes? E se recebidas fosse 40?
22. Um cliente tem dois serviços e faturamento anual de R$ 100 mil. Um JOIN devolveu duas linhas com esse faturamento. Qual seria o erro de somar as duas?
23. Calcule média e mediana de `100, 200, 3000`. Qual delas foi mais afetada pelo maior valor?
24. Um usuário modifica o frontend para exibir o botão de administrador. Isso deve bastar para executar uma rota administrativa? Explique.
25. O servidor terminou a gravação, mas a resposta não chegou ao navegador. É seguro reenviar imediatamente? O que conferir primeiro?
26. Você obteve média de `1135000` na rota `/testar`. Isso contradiz a média `2261250` da planilha? Justifique.
27. A prévia mostra dados após F5. Isso prova que eles estão no PostgreSQL?
28. O projeto informa média e mediana próximas. Isso prova que a distribuição é equilibrada ou normal?

## 17.3 Desafios práticos

29. Localize no código uma action, um getter e um state. Explique cada um sem ler o comentário.
30. Siga o campo `faturamentoAnual` desde o JSON até o cartão de média. Identifique a troca para snake_case.
31. Encontre a restrição que impede repetir o mesmo código no mesmo lote e explique por que outro lote pode usar esse código.
32. Faça uma consulta SQL que conte contratos de um lote sem contar contratos dos demais.
33. Provoque uma falha de chave apenas em ambiente de teste e explique a resposta esperada, sem divulgar a chave real.
34. Compare o número de erros com o de linhas inválidas e explique por que eles podem divergir.

# 18 Gabarito comentado

## 18.1 Respostas das questões conceituais

1. Pinia compartilha e organiza estado e operações no frontend. PostgreSQL persiste dados no servidor; store em memória não é persistência definitiva.
2. `ref` guarda estado; `computed` produz getters; funções retornadas são actions.
3. `computed` representa valor derivado; `watch` executa efeito quando algo observado muda.
4. Cria refs para dados reativos da store, preservando acesso reativo ao desestruturar.
5. `importarPlanilha` lê; `validarNoBackend` envia para processamento e salvamento.
6. O cliente pode ser modificado ou ignorado. A API precisa proteger sua própria entrada.
7. DTO transporta dados; entidade representa persistência mapeada; service aplica ou coordena regras.
8. Axios faz HTTP; interceptadores acrescentam autenticação e tratam respostas de modo centralizado.
9. JWT autentica usuário no backend; a chave ETL é compartilhada entre backend e Python.
10. Python, via `banco.criar_lote` chamado por `main.processar_lote`.
11. Entrada JSON em `api.py`; transformação em `limpeza.py`, `analise.py` e `insights.py`; carga em `banco.py`.
12. FastAPI define a aplicação e as rotas; Uvicorn executa o servidor ASGI.
13. DataFrame é estrutura tabular; Series é uma sequência, como uma coluna.
14. Um cliente pode contratar vários serviços, gerando várias associações.
15. Commit confirma a transação; rollback desfaz suas alterações quando ocorre falha.
16. O lote foi criado antes, mas a transação de clientes e análises falhou e foi desfeita.
17. Média resume pela soma dividida pela contagem; mediana usa a posição central; moda usa frequência.
18. Amostral, com `ddof=1`; com um único cliente o resultado é nulo.
19. Não. Associação observada não comprova relação causal nem generalização automática.
20. O JOIN pode multiplicar linhas e repetir o faturamento do mesmo cliente; a agregação deve respeitar a unidade de análise.

## 18.2 Respostas de raciocínio e prática

21. Sim: 32 + 6 = 38. Com 40, o contrato não fecha e deve ser recusado.
22. O total sairia R$ 200 mil, duplicando o faturamento real do cliente.
23. Média = 1100; mediana = 200. A média foi muito elevada pelo maior valor.
24. Não. O backend deve conferir o perfil autenticado e negar autorização inadequada.
25. Consulte o histórico e o lote antes. Reenviar pode criar outro lote.
26. Não. São amostras diferentes: JSON de dois clientes e planilha de 38 linhas.
27. Não. Dados podem ter sido restaurados de `sessionStorage`.
28. Não. Duas medidas semelhantes não comprovam simetria, normalidade ou ausência de concentração.
29. Exemplo: state `clientes`, getter `totalValidas`, action `importarPlanilha`.
30. Java envia camelCase; Pydantic aceita aliases; análise usa `faturamento_anual`; a média vai em `resumo.media`; Vue formata o cartão.
31. `UNIQUE(lote_id, codigo_cti)` valida a combinação; mudar lote muda a combinação.
32. Use JOIN de contrato com cliente e filtro `cliente.lote_id`, como na seção de comandos.
33. O Python deve recusar autenticação interna; o backend traduz a falha para a interface. Confira logs e status sem expor valores.
34. Uma linha pode conter mais de uma ocorrência de erro.

# 19 Plano de revisão e leitura do código

## 19.1 Revisão em cinco sessões

| Sessão | Foco | Evidência de que você aprendeu |
|---|---|---|
| 1 | Arquitetura, pastas e responsabilidades | Desenhar o fluxo sem consultar o guia |
| 2 | Vue, Pinia, state, getters e actions | Explicar uma action real e seus estados de carregamento |
| 3 | Java, HTTP, autenticação e contrato | Descrever o pedido e a resposta do upload |
| 4 | Python, estatística e banco | Explicar média, rollback e clientes versus contratos |
| 5 | Demonstração e simulado | Executar a amostra e responder sem consultar o gabarito |

Em cada sessão, leia o trecho real do programa, faça uma pequena anotação com suas próprias palavras e explique em voz alta. Se precisar ler cada linha para responder, volte ao fluxo de entrada, processamento e saída.

## 19.2 Ordem sugerida de arquivos

1. `FrontEnd/src/main.js` e `FrontEnd/src/router/index.js`.
2. `FrontEnd/src/stores/uploadStore.js` e `FrontEnd/src/utils/planilha.js`.
3. `FrontEnd/src/services/api.js` e `FrontEnd/src/stores/auth.js`.
4. `BackEnd/src/main/java/com/cti/provedor/controller/UploadController.java`.
5. Services `UploadService`, `ClienteValidacaoService` e `PythonEtlService`.
6. `analytics-python/api.py` e `analytics-python/main.py`.
7. `analytics-python/limpeza.py`, `analise.py` e `insights.py`.
8. `analytics-python/banco.py`, `config.py` e o SQL de analytics.
9. `BackEnd/src/main/java/com/cti/provedor/service/DashboardService.java`.
10. `FrontEnd/src/stores/dashboard.js` e `FrontEnd/src/components/AnalyticsPanel.vue`.

## 19.3 Glossário de revisão rápida

| Termo | Definição curta |
|---|---|
| API | Interface de comunicação entre programas |
| Endpoint | Operação acessível por rota e método HTTP |
| Payload | Dados transportados em um pedido ou resposta |
| Store | Unidade de estado e operações do Pinia |
| State | Informação guardada pela aplicação |
| Getter | Informação calculada a partir do estado |
| Action | Operação da store que executa uma tarefa |
| Promise | Representação de resultado assíncrono futuro |
| DTO | Estrutura de transferência de dados |
| JWT | Token assinado utilizado na autenticação |
| Hash | Representação calculada usada, neste caso, para verificar senha |
| ETL | Receber, transformar e carregar dados |
| Driver | Biblioteca que implementa comunicação com o banco |
| Engine | Objeto SQLAlchemy que gerencia acesso e conexões |
| Schema | Namespace de tabelas e outros objetos do banco |
| JOIN | Operação que combina registros relacionados |
| Transação | Conjunto de operações confirmado ou desfeito em bloco |
| Lote | Registro de uma importação com identidade e status |
| Build | Processo que prepara os artefatos para execução ou publicação |
| Deploy | Disponibilização da aplicação em um ambiente de execução |

# 20 Checklist de apresentação e entrega

- [ ] Sei explicar o objetivo em até um minuto.
- [ ] Sei indicar a função de `FrontEnd`, `BackEnd`, `analytics-python` e `deploy`.
- [ ] Consigo mostrar state, getter e action em uma store real.
- [ ] Sei diferenciar leitura local, envio ao backend e persistência.
- [ ] Sei explicar por que a validação do navegador não basta.
- [ ] Sei dizer quem cria o lote no contrato atual.
- [ ] Consigo explicar DTO, service, controller e repository.
- [ ] Sei distinguir JWT, chave ETL, CORS e hash de senha.
- [ ] Sei explicar o papel de pandas, FastAPI, Uvicorn, SQLAlchemy e psycopg.
- [ ] Consigo justificar média, mediana, desvio amostral e limites da correlação.
- [ ] Sei explicar por que 32 clientes podem gerar 61 contratos.
- [ ] Sei demonstrar commit, rollback e registro de erro no histórico.
- [ ] Tenho a amostra correta e conheço os resultados esperados.
- [ ] Conferi as configurações sem exibir segredos.
- [ ] Preparei os três terminais e sei a pasta de cada comando.
- [ ] Executei os testes e verifiquei se o teste integrado não foi pulado.
- [ ] Conferi o mesmo lote no banco e no dashboard.
- [ ] Sei informar quando uso execução anterior ou dados de exemplo.
- [ ] Preenchi meu nome, RA e turma na cópia de entrega.
- [ ] Revisei os arquivos alterados e as exigências específicas da professora.

**Base deste guia:** leitura dos arquivos locais citados, das stores, dos services Java, dos arquivos Python, do SQL e das configurações de execução. O documento descreve a implementação observada em 12 de novembro de 2026. Se o código mudar, revise o fluxo e os comandos correspondentes antes da apresentação.
