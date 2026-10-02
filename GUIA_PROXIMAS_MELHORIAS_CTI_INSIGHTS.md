---
title: "Guia de estudos das próximas melhorias do CTI Insights"
subtitle: "Projeto Integrador Individual CTI SENAI"
lang: pt-BR
---

# 1. Para que serve este guia

Este material mostra um caminho de estudo para evoluir o CTI Insights sem tentar construir tudo de uma vez. Você já tem uma aplicação em Vue que apresenta o projeto, lê uma planilha no navegador, mostra erros, lista clientes e gera indicadores. O próximo passo é torná-la mais confiável e mais fácil de explicar na apresentação.

**Como estudar:** faça uma etapa por vez. Antes de mudar um arquivo, leia-o e anote o que ele faz. Depois da mudança, execute o projeto e teste um caso normal e um caso com erro. Registre em um commit pequeno quando a etapa funcionar.

## 1.1 Mapa simples do projeto

| Parte | Arquivo principal | O que faz |
|---|---|---|
| Rotas | `FrontEnd/src/router/index.js` | Decide qual tela aparece em cada endereço. |
| Upload | `FrontEnd/src/views/UploadView.vue` | Recebe o arquivo escolhido pelo usuário. |
| Leitura e validação | `FrontEnd/src/utils/planilha.js` | Lê as linhas da planilha e identifica erros. |
| Números e textos | `FrontEnd/src/utils/formato.js` | Padroniza textos e interpreta valores. |
| Dados compartilhados | `FrontEnd/src/composables/useClientes.js` | Entrega a mesma carteira às telas. |
| Relatório e gráficos | `FrontEnd/src/views/RelatoriosView.vue` e `GraficosView.vue` | Exibem os resultados. |

**Vocabulário:** *componente* é uma parte da interface; *rota* é um endereço da aplicação; *estado* são dados que podem mudar na tela; *validação* é a conferência das regras de entrada; *build* é a montagem da versão de produção.

# 2. Preparação e diagnóstico

Abra um terminal PowerShell na pasta `C:\Users\Instrutor\CTI_SENAI`. Nesta máquina, use `npm.cmd`, pois `npm` pode chamar um script PowerShell bloqueado pela política de execução.

```powershell
git status --short
node --check FrontEnd/src/router/index.js
npm.cmd run dev
```

**Saída esperada após corrigir o roteador:** `node --check` termina sem mensagem de erro. O Vite mostra um endereço local, geralmente `http://localhost:5173/`. A porta pode ser outra se 5173 estiver ocupada.

**Atenção ao estado atual:** a primeira linha de `FrontEnd/src/router/index.js` aparece como `i mport`, com um espaço indevido. Isso causa `SyntaxError: Unexpected identifier 'mport'`. O arquivo já tinha essa alteração local quando foi analisado. Corrija para `import` e confira novamente. O build completo também encontrou uma falha local ao carregar uma dependência nativa do Tailwind; se ela continuar após a correção, trate-a como um problema separado de instalação/ambiente.

# 3. Roteiro das próximas melhorias

## 3.1 Etapa 1 Corrigir o roteador e confirmar o fluxo

**O que aprender:** diferença entre erro de sintaxe e erro de funcionamento. Um erro de sintaxe impede o JavaScript de começar; um erro de funcionamento aparece depois que a aplicação inicia.

1. Corrija a palavra `import` na linha 1 do roteador.
2. Execute `node --check FrontEnd/src/router/index.js`.
3. Inicie com `npm.cmd run dev` e visite `/`, `/login`, `/app/upload`, `/app/relatorios` e `/app/graficos`.
4. Rode `npm.cmd run build` quando o ambiente estiver funcionando.

**Pronto quando:** todas as rotas abrem sem tela branca e o terminal não mostra erro de sintaxe. Não altere várias telas ao mesmo tempo nesta etapa.

## 3.2 Etapa 2 Validar o arquivo antes de processar

Hoje o campo de seleção sugere `.xlsx`, `.xls` e `.csv`, mas um arquivo arrastado pode chegar sem essa verificação. Crie uma função em `UploadView.vue` que confira a extensão e um limite de tamanho definido por você. Use a mesma função na seleção e no arrastar e soltar. Mostre uma mensagem simples, como “Escolha um arquivo .xlsx, .xls ou .csv”.

**O que aprender:** `File`, `name`, `size`, eventos de formulário e uma função reutilizada em dois caminhos de entrada.

**Teste:** escolha um `.xlsx` válido, depois arraste um `.txt`. O primeiro deve poder ser processado; o segundo deve ser recusado antes da leitura. A extensão é uma primeira barreira de experiência, não uma garantia de segurança.

## 3.3 Etapa 3 Tornar as mensagens de erro úteis

Em `planilha.js`, a linha de cabeçalho é aceita quando apenas dois campos conhecidos aparecem. Isso pode deixar passar uma planilha que não contém colunas essenciais. Defina quais colunas o projeto realmente precisa para funcionar e, depois de montar `mapa`, informe exatamente quais estão faltando. Comece por `id`, `empresa`, `consultor`, `faturamento` e `servico`; ajuste a lista ao contrato de dados que você decidir usar.

**O que aprender:** entrada inválida, mensagem orientada à solução e separação entre erro de arquivo e erro de uma linha da planilha.

**Teste:** importe uma planilha sem a coluna “Empresa”. A mensagem deve citar “Empresa”, em vez de produzir um relatório vazio ou incorreto.

## 3.4 Etapa 4 Criar testes para as regras de dados

As funções em `formato.js` são um bom começo porque recebem um valor e devolvem outro. Teste exemplos como `Saúde`/`SAUDE`, `R$ 1.200,50`, `850 mil`, valor negativo e texto sem número. Depois teste `interpretarData` com uma data válida e `31/02/2026`. Por fim, use a planilha oficial e confira o resumo esperado: **300 linhas, 265 válidas, 35 com erro e 37 ocorrências**.

**O que aprender:** teste automatizado compara o resultado real com um resultado esperado; teste manual confere o fluxo na interface. Os dois se complementam. Uma ferramenta possível é o executor de testes do próprio Node para funções puras; escolha a ferramenta depois de conseguir escrever os primeiros casos.

**Pronto quando:** uma mudança acidental na interpretação de números ou datas faz pelo menos um teste falhar. Não copie a implementação para dentro do teste; use exemplos de entrada e saída.

## 3.5 Etapa 5 Melhorar a tabela de relatórios

A tela já permite pesquisar e filtrar linhas com erro. Para a próxima versão, mostre a quantidade de resultados com clareza e adicione um estado vazio explicativo. Depois, se a tabela ficar difícil de navegar com 300 linhas, implemente paginação simples: 20 ou 25 linhas por página e botões “Anterior” e “Próxima”. A paginação deve vir **depois** da busca e do filtro de erros.

**O que aprender:** `computed`, ordem das transformações de uma lista e estado da interface. Quando a busca mudar, volte à primeira página.

**Teste:** pesquise um cliente, ative o filtro de erros e confira se a contagem e a página exibida continuam corretas.

## 3.6 Etapa 6 Deixar os dados de exemplo inconfundíveis

Sem upload, relatórios e gráficos usam cinco clientes de exemplo. O aviso atual ajuda, mas você pode reforçá-lo no título do gráfico e nos indicadores da tela interna. Isso evita que números de demonstração sejam apresentados como números da carteira real.

**O que aprender:** diferença entre dado de demonstração e dado importado; comunicação honesta na interface.

**Teste:** abra gráficos antes de importar; depois importe a planilha e observe a troca de fonte e valores. Use “Descartar planilha” e confira se o aviso de exemplo reaparece.

## 3.7 Etapa 7 Atualizar a documentação

O `FrontEnd/README.md` ainda descreve principalmente a landing page. Atualize-o com as rotas, o fluxo de upload, os formatos aceitos, a planilha de teste, os comandos e as limitações atuais. Escreva também que o login e o contato são demonstrativos nesta versão.

**O que aprender:** documentação é parte do produto. Outra pessoa deve conseguir rodar e testar o sistema sem pedir explicações a você.

# 4. Melhorias para uma fase posterior

**Autenticação real:** o formulário de login hoje apenas redireciona para `/app/upload`; o roteador permite abrir `/app` diretamente. Quando houver um back-end definido, planeje usuários, sessão e proteção das rotas junto com ele. Esconder um link no front-end não protege dados.

**Contato real:** o formulário da página inicial mostra “Mensagem registrada”, mas não envia nem salva a mensagem. Até existir um serviço de recebimento, prefira uma mensagem que deixe claro que é uma demonstração.

**Persistência:** `sessionStorage` mantém a importação durante a sessão do navegador. Para guardar carteiras entre sessões e permitir vários usuários, será necessário definir armazenamento no back-end, permissões e regras de privacidade dos dados dos clientes.

**Conceito de faturamento:** alinhe com o professor e com a planilha se o valor é mensal, anual ou uma faixa estimada. Hoje os rótulos da interface usam “mensal”, enquanto o código também reconhece cabeçalhos com “anual”. Uma mesma grandeza precisa ter o mesmo significado na planilha, no cálculo e no gráfico.

# 5. Erros comuns e soluções

| Situação | Causa provável | Primeiro passo |
|---|---|---|
| `Unexpected identifier 'mport'` | Espaço na palavra `import` do roteador. | Corrigir a linha 1 e rodar `node --check`. |
| `npm.ps1 não pode ser carregado` | Política do PowerShell bloqueia o atalho. | Usar `npm.cmd run dev`. |
| Tela branca | Erro de JavaScript ou importação. | Ler o terminal do Vite e o console do navegador. |
| Planilha não reconhecida | Cabeçalho diferente ou arquivo inválido. | Conferir nomes das colunas e testar com o modelo em `Data/`. |
| Números diferentes do esperado | Coluna errada, formato numérico ou linhas inválidas incluídas. | Conferir o resumo 300/265/35/37 e verificar `formato.js`. |
| Build falha antes de analisar o código | Dependência nativa ou ambiente de instalação. | Registrar a primeira mensagem de erro e verificar dependências antes de alterar o código. |

# 6. Comandos para praticar

Na raiz do projeto:

```powershell
git status --short
node --check FrontEnd/src/router/index.js
npm.cmd run dev
npm.cmd run build
```

Se as dependências ainda não estiverem instaladas, o atalho do projeto é `npm.cmd run setup`. Para testar a importação, abra o endereço mostrado pelo Vite, vá a Upload e selecione `Data/CTI_Insights_Base_Validacao_300_Registros.xlsx`.

**Saída esperada da planilha oficial:** 300 linhas lidas; 265 válidas; 35 com erro; 37 ocorrências. No build, o sinal de sucesso é uma linha final semelhante a `built in ...` e arquivos na pasta `FrontEnd/dist/`. Os tempos e tamanhos variam entre máquinas.

# 7. Checklist de estudo e entrega

- [ ] Corrigi o roteador e confirmei as cinco rotas.
- [ ] Consigo explicar o caminho do arquivo desde o upload até os gráficos.
- [ ] Testei um arquivo aceito e um arquivo recusado.
- [ ] Conferi a base oficial e o resumo 300/265/35/37.
- [ ] Escrevi testes para pelo menos uma regra de número e uma regra de data.
- [ ] Deixei claro na interface quando aparecem dados de exemplo.
- [ ] Atualizei o README com o funcionamento atual.
- [ ] Sei explicar o que é demonstrativo: login, contato e armazenamento da sessão.
- [ ] Rodei `git status --short` para revisar o que será entregue.

**Ordem recomendada:** termine as etapas 1 a 4 antes de ampliar funções. Assim você constrói uma base que inicia, recebe arquivos com cuidado e detecta regressões nos cálculos.
