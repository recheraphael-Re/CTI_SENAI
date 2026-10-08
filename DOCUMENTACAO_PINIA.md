---
title: Documentação do Pinia no CTI Insights
lang: pt-BR
---

**Aluno:** ____________________________________  
**RA:** _______________________________________  
**Curso e turma:** ______________________________  
**Data:** 8 de outubro de 2026

# 1 Objetivo

Esta documentação explica como o CTI Insights utiliza o Pinia para compartilhar os dados de clientes entre telas Vue. A store de upload centraliza a importação de planilhas, os resultados da validação e os indicadores. Assim, diferentes componentes consultam a mesma base sem repetir o processamento do arquivo.

O Pinia gerencia o estado do frontend. O processamento de planilhas deste projeto funciona no navegador, sem depender de backend. O login e o cadastro atuais são demonstrativos e não oferecem autenticação real.

# 2 Conceitos fundamentais

Uma store reúne dados compartilhados e as operações que os modificam. Neste projeto foi adotado o formato Setup Store, compatível com a Composition API do Vue. Nesse formato, os valores criados com `ref` representam o estado, os valores criados com `computed` representam getters e as funções representam actions. Esses elementos precisam ser retornados pela função da store para ficarem acessíveis aos consumidores. [1]

| Conceito | Responsabilidade | Exemplo do projeto |
|---|---|---|
| Estado | Manter dados que mudam durante o uso | `clientes`, `nomeArquivo`, `carregando` |
| Getter | Calcular informações a partir do estado | `totalValidas`, `faturamentoTotal` |
| Action | Executar operações e alterar o estado | `importarPlanilha`, `limpar` |

O Pinia permite compartilhar estado entre componentes e páginas. Ele não substitui uma API, um banco de dados ou a validação de permissões no servidor. [2]

# 3 Instalação e inicialização

O arquivo `FrontEnd/package.json` já declara a dependência `pinia`. Para instalar as dependências existentes e iniciar o projeto, execute na raiz:

```powershell
cd FrontEnd
npm.cmd install
npm.cmd run dev
```

O resultado esperado é o Vite mostrar o endereço local da aplicação. Use o endereço exibido no terminal, pois a porta pode variar. No PowerShell, `npm.cmd` evita a dependência da execução do script `npm.ps1`.

A inicialização está em `FrontEnd/src/main.js`:

```javascript
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

O plugin do Pinia é registrado antes da montagem da aplicação, tornando as stores disponíveis aos componentes.

# 4 Organização dos arquivos

| Arquivo em FrontEnd/src | Papel |
|---|---|
| `main.js` | Registrar o Pinia na aplicação |
| `stores/uploadStore.js` | Definir a store principal de upload |
| `router/store/uploadstore.js` | Reexportar a store para compatibilidade com o caminho antigo |
| `composables/useClientes.js` | Expor referências reativas e actions da store |
| `views/UploadView.vue` | Selecionar e processar planilhas |
| `views/RelatoriosView.vue` | Consumir dados para relatórios |
| `views/GraficosView.vue` | Consumir dados para gráficos e indicadores |

O arquivo de compatibilidade não cria uma segunda store. A implementação central permanece em `stores/uploadStore.js`.

# 5 Estado da store de upload

A store é definida com `defineStore('upload', () => { ... })`. O identificador `upload` distingue esta store dentro da aplicação.

| Estado | Conteúdo |
|---|---|
| `arquivo` | Objeto do arquivo selecionado, mantido com `shallowRef` |
| `clientes` | Registros normalizados, incluindo os registros inválidos |
| `dadosOriginais` | Dados lidos antes da apresentação dos resultados |
| `erros` e `resumo` | Ocorrências de validação e contagens da importação |
| `nomeArquivo` e `dataUpload` | Nome do arquivo e momento do processamento |
| `totalColunas` e `periodoFaturamento` | Metadados da planilha |
| `carregando` e `erro` | Estado da operação e mensagem de falha |

`shallowRef` mantém a referência ao objeto File sem tornar suas propriedades profundamente reativas. A variável interna `operacao` controla o processamento: quando a base é limpa, um resultado pendente de uma operação anterior não deve voltar a preencher os dados.

# 6 Getters e indicadores

Os getters são declarados com `computed`. Eles acompanham as mudanças das dependências reativas e oferecem informações derivadas do estado.

`temDados` verifica se há clientes importados. `usandoExemplo` indica a ausência dessa base. `clientesExibidos` devolve a base importada ou cinco registros demonstrativos. `clientesValidos` filtra os registros válidos dessa lista para calcular os indicadores.

O getter `faturamentoTotal` soma o faturamento dos clientes válidos, enquanto `faturamentoPorSegmento` agrupa os valores por segmento e seleciona os dez maiores totais. `indicadores` apresenta a quantidade de clientes válidos, o faturamento médio e os registros ativos.

Há uma diferença prática entre os getters: `totalValidas` conta apenas registros efetivamente importados; `clientesValidos` pode utilizar a amostra. Sem uma importação, `totalValidas` é zero, mesmo que os indicadores exibam dados de demonstração. Além disso, erros são ocorrências: uma única linha pode apresentar mais de um erro.

# 7 Actions e fluxo de importação

1. `selecionarArquivo(novo)` aceita arquivos com extensão XLSX, XLS ou CSV e rejeita extensões diferentes. Durante o carregamento, uma nova seleção é bloqueada.
2. `processarPlanilha()` encaminha o arquivo selecionado para `importarPlanilha(arquivoSelecionado)`.
3. `importarPlanilha` valida a seleção e ativa `carregando`.
4. `lerPlanilha`, em `utils/planilha.js`, realiza a leitura e devolve registros, erros, resumo e metadados.
5. A action atualiza o estado e tenta gravar os resultados em `sessionStorage`.
6. Os getters refletem a base atualizada e as telas consumidoras acompanham as mudanças.

O retorno da importação é `true` em caso de sucesso e `false` em caso de falha ou operação descartada. A action informa a falha por `erro` e encerra o carregamento da operação vigente no bloco `finally`. A extensão aceita não garante que o conteúdo da planilha seja válido.

`limpar()` esvazia a base, remove a persistência da sessão e invalida resultados pendentes. Como esta é uma Setup Store, a limpeza do projeto foi implementada explicitamente; os componentes devem chamar `store.limpar()`.

# 8 Uso em componentes Vue

Para desestruturar estado e getters preservando a reatividade, utilize `storeToRefs`. Actions podem ser chamadas diretamente pela store. [1]

Exemplo didático baseado nos nomes reais da implementação:

```vue
<script setup>
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore.js'

const store = useUploadStore()
const { totalValidas, carregando, erro } = storeToRefs(store)
</script>

<template>
  <p>Clientes válidos importados: {{ totalValidas }}</p>
  <p v-if="erro" role="alert">{{ erro }}</p>
  <button :disabled="carregando" @click="store.limpar()">
    Limpar base
  </button>
</template>
```

Dentro da definição da store, usa-se `.value` para acessar refs em JavaScript. No acesso direto pela store, o Pinia disponibiliza os valores sem exigir `.value`. No template, as refs também são desembrulhadas pelo Vue.

# 9 Persistência e integração futura

A persistência é implementada pelo próprio projeto com a chave `cti-insights-clientes-v2`. Não é um recurso automático de toda store Pinia. Uma atualização da página na mesma sessão pode restaurar os resultados salvos. Ao recuperar o JSON, `dataInicio` é reconvertida para Date. O objeto File não é restaurado e deve ser selecionado novamente quando necessário.

Se a gravação exceder a cota ou estiver indisponível, o processamento continua em memória. A implementação tenta remover uma base antiga para não restaurar resultados desatualizados. `sessionStorage` não oferece armazenamento centralizado ou compartilhamento entre usuários.

Para uma futura integração de cadastro e login, uma action poderá chamar a API e guardar o estado necessário da sessão conforme a arquitetura escolhida. As permissões e a autenticação real devem ser verificadas pelo backend. Atualmente, `CadastroView.vue` utiliza refs locais, valida o formulário e não grava senhas nem cria contas. Não existe uma store de autenticação implementada neste projeto.

# 10 Verificação e resultados esperados

Na pasta `FrontEnd`, execute:

```powershell
npm.cmd test
npm.cmd run build
```

Caso o executor de testes apresente `spawn EPERM` no ambiente restrito, a alternativa utilizada nesta verificação foi:

```powershell
node --test --test-isolation=none tests/*.test.js
```

Resultado observado em 8 de outubro de 2026: sete testes passaram, sem falhas. A opção acima executa os testes sem isolamento por processo. O teste da store, quando a base oficial está presente, verifica 300 registros importados, 265 clientes válidos e 37 ocorrências de erro.

Para reproduzir a apresentação, abra `/app/upload`, selecione `Data/CTI_Insights_Base_Validacao_300_Registros.xlsx` a partir da raiz do projeto e processe a planilha. Consulte as telas de relatórios e gráficos. Atualize a página para conferir a recuperação da sessão e limpe a base para conferir o retorno ao estado inicial.

# 11 Erros comuns e soluções

| Problema | Solução |
|---|---|
| Store utilizada antes de registrar o Pinia | Registrar `app.use(createPinia())` antes da montagem |
| Valor desestruturado deixa de acompanhar mudanças | Usar `storeToRefs(store)` para estado e getters |
| Nenhum arquivo selecionado ou extensão rejeitada | Selecionar XLSX, XLS ou CSV antes de processar |
| Esperar que File reapareça após atualizar a página | Selecionar novamente o arquivo quando necessário |
| Confundir amostra com importação real | Conferir `usandoExemplo` e `temDados` |
| Confundir linhas inválidas com ocorrências de erro | Comparar `totalComErro` com `totalErros` |
| Tentar usar Pinia para criar uma conta real sem API | Implementar cadastro e autenticação no backend |

# 12 Checklist de entrega

- [ ] Preencher nome, RA, curso e turma no Word.
- [ ] Explicar a diferença entre estado, getters e actions.
- [ ] Identificar a store e os componentes que a utilizam.
- [ ] Demonstrar importação, indicadores e limpeza da base.
- [ ] Explicar os limites do armazenamento em sessão.
- [ ] Conferir os testes e a compilação no ambiente de apresentação.
- [ ] Entregar o arquivo Word na plataforma indicada pelo professor.

# 13 Referências

[1] Pinia. Defining a Store. Documentação oficial. https://pinia.vuejs.org/core-concepts/ . Acesso em 8 de outubro de 2026.

[2] Pinia. Introduction. Documentação oficial. https://pinia.vuejs.org/introduction.html . Acesso em 8 de outubro de 2026.

[3] Código-fonte do CTI Insights: `FrontEnd/src/main.js`, `FrontEnd/src/stores/uploadStore.js`, componentes consumidores e `FrontEnd/tests/uploadStore.test.js`. Consultado em 8 de outubro de 2026.
