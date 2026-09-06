---
title: "Guia de Estudos — Sistema CTI Insights"
subtitle: "Projeto Integrador 2026/01 · Framework Front-End"
author: "Raphael Reche"
date: "06/09/2026"
lang: pt-BR
---

# 1. Visão geral

## 1.1 O problema do cliente

O Provedor **CTI Comunicação de Dados e Tecnologia** mantém a carteira de clientes
em planilhas Excel preenchidas à mão: consultor, código do cliente, segmento,
nível (A/B/C), faturamento e serviços contratados. O preenchimento manual gera
inconsistências — o mesmo segmento aparece como `Saúde`, `SAUDE` e `saude` — e
não existe nenhuma análise em cima disso.

O **CTI Insights** resolve as duas pontas: padroniza os dados e transforma a
planilha em indicadores.

## 1.2 Arquitetura prevista

```
Excel  →  Vue 3 + Vite  →  Spring Boot  →  Python/Pandas  →  PostgreSQL  →  Chart.js
          (este repo)       (Azure)         (Ciência de Dados)
```

**Neste repositório está só o front-end.** As pastas `BackEnd/`, `CienciaD/` e
`Database/` são escopo de outras disciplinas e ainda estão vazias.

Como o back-end ainda não existe, o front foi construído para funcionar
sozinho: ele lê a planilha no próprio navegador. Isso não é um desvio da
arquitetura — é o que a disciplina de Framework Front-End cobra, e garante que
a apresentação funcione mesmo que o Spring Boot não esteja pronto no dia.

## 1.3 As telas

| Rota | Tela | Layout |
|---|---|---|
| `/` | Apresentação (site público) | `PublicLayout` |
| `/login` | Login | `PublicLayout` |
| `/app/upload` | Envio da planilha | `AppLayout` |
| `/app/relatorios` | Tabela da base tratada | `AppLayout` |
| `/app/graficos` | Indicadores e gráfico | `AppLayout` |

---

# 2. Como rodar

## 2.1 Comandos

Da raiz do repositório (`CTI_SENAI`):

```bash
npm run setup    # instala as dependências (só na primeira vez)
npm run dev      # sobe o servidor de desenvolvimento
npm run build    # gera a versão de produção
npm run preview  # serve a versão de produção, para conferir antes de publicar
```

Esses comandos da raiz são atalhos: o `package.json` de verdade está em
`FrontEnd/`. Se preferir, `cd FrontEnd` e rode os mesmos comandos lá.

## 2.2 Saída esperada do `npm run dev`

```
> cti-insights@0.0.0 dev
> npm --prefix FrontEnd run dev

  VITE v8.2.1  ready in 829 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

## 2.3 Saída esperada do `npm run build`

```
dist/index.html                           0.75 kB │ gzip:   0.43 kB
dist/assets/index-*.css                  30.71 kB │ gzip:   6.13 kB
dist/assets/CartaoIndicador-*.js          1.16 kB │ gzip:   0.61 kB
dist/assets/GraficosView-*.js             1.64 kB │ gzip:   0.92 kB
dist/assets/LoginView-*.js                1.84 kB │ gzip:   0.82 kB
dist/assets/RelatoriosView-*.js           4.23 kB │ gzip:   1.82 kB
dist/assets/UploadView-*.js               4.86 kB │ gzip:   2.05 kB
dist/assets/useClientes-*.js              8.65 kB │ gzip:   3.48 kB
dist/assets/index-*.js                  273.14 kB │ gzip:  98.22 kB
dist/assets/xlsx-*.js                   492.52 kB │ gzip: 160.52 kB

✓ built in 446ms
```

**Repare em três coisas**, porque elas provam decisões de projeto:

1. Cada view virou um arquivo separado — é o *lazy loading* das rotas.
2. `useClientes` e `CartaoIndicador` viraram arquivos próprios porque são
   **compartilhados** por mais de uma view.
3. `xlsx` está sozinho em 492 kB — ele só é baixado quando alguém envia uma
   planilha, e não no primeiro acesso ao site.

## 2.4 Teste manual do fluxo completo

1. Abrir <http://localhost:5173/>
2. Ir em **Acessar sistema** → Login → **Entrar**
3. Na tela de Upload, arrastar `Data/CTI_Insights_Base_Validacao_300_Registros.xlsx`
4. Clicar em **Enviar e processar**

Resultado esperado no resumo da importação:

| Linhas lidas | Válidas | Com erro | Ocorrências |
|---|---|---|---|
| 300 | 265 | 35 | 37 |

---

# 3. Estrutura de pastas

```
FrontEnd/src/
├── App.vue                    # só um <RouterView />
├── main.js                    # cria o app e monta o router
├── router/index.js            # as rotas
├── layouts/
│   ├── PublicLayout.vue       # moldura do site público
│   └── AppLayout.vue          # moldura do painel interno
├── components/
│   ├── SiteHeader.vue         # cabeçalho público
│   ├── NavMenu.vue            # menu do site público
│   ├── SiteFooter.vue         # rodapé
│   ├── Sidebar.vue            # menu lateral do painel
│   ├── Header.vue             # cabeçalho do painel
│   ├── ServiceCard.vue        # cartão de serviço da Home
│   ├── CartaoIndicador.vue    # cartão de número (KPI)
│   ├── RevenueChart.vue       # gráfico de barras
│   └── ThemeToggle.vue        # botão claro/escuro
├── composables/
│   ├── useTheme.js            # estado do tema
│   └── useClientes.js         # estado da carteira de clientes
├── utils/
│   ├── planilha.js            # leitura e validação do Excel
│   └── formato.js             # números e moeda
└── views/                     # uma por rota
```

**A regra que organiza isso:** `components/` são peças reaproveitáveis,
`layouts/` são molduras que contêm um `<RouterView />`, `views/` são telas
ligadas a uma rota, `composables/` guardam estado e lógica sem interface, e
`utils/` são funções puras que não sabem que o Vue existe.

---

# 4. Roteamento

## 4.1 Duas molduras

O sistema tem dois "mundos" com navegação diferente:

- **Site público** (`/`, `/login`): barra no topo com menu hambúrguer no celular.
- **Painel interno** (`/app/*`): barra lateral retrátil.

Isso é resolvido com **rotas aninhadas**: a rota-pai define o layout, e as
rotas-filhas entram no `<RouterView />` dele.

```js
const routes = [
  {
    path: '/',
    component: PublicLayout,
    children: [
      { path: '', name: 'home', component: HomeView },
      { path: 'login', name: 'login', component: LoginView }
    ]
  },
  {
    path: '/app',
    component: AppLayout,
    redirect: '/app/upload',
    children: [
      { path: 'upload', name: 'upload', component: UploadView },
      { path: 'relatorios', name: 'relatorios', component: RelatoriosView },
      { path: 'graficos', name: 'graficos', component: GraficosView }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]
```

Por isso o `App.vue` tem só um `<RouterView />`: quem decide a moldura é a rota.

## 4.2 Conceitos para saber explicar

**Lazy loading.** Só a `HomeView` é importada normalmente (é a porta de
entrada). As outras usam `() => import('...')`, que faz o Vite gerar um arquivo
separado, baixado só quando a rota é acessada. É o que aparece no `npm run build`.

**Catch-all.** `/:pathMatch(.*)*` pega qualquer rota que não existe e manda
para a home, evitando tela em branco.

**`createWebHistory`.** Deixa as URLs limpas (`/app/upload` em vez de
`/#/app/upload`), mas exige configuração no servidor de hospedagem — veja a
seção 12.

**`scrollBehavior`.** Rola até a âncora quando a URL tem `#contato`; senão,
volta ao topo a cada troca de tela.

---

# 5. Componentes: props e eventos

## 5.1 Props — dados descendo

`CartaoIndicador.vue` recebe tudo por propriedade:

```js
const props = defineProps({
  rotulo: { type: String, required: true },
  valor: { type: [String, Number], required: true },
  tom: {
    type: String,
    default: 'marca',
    validator: (valor) => ['marca', 'neutro', 'ok', 'erro', 'aviso'].includes(valor)
  },
  tamanho: { type: String, default: 'grande' },
  caixa: { type: Boolean, default: true }
})
```

E é usado nos dois lugares que precisam de um número com rótulo:

```html
<!-- GraficosView: KPI dentro de uma caixa -->
<CartaoIndicador :rotulo="kpi.rotulo" :valor="kpi.valor" />

<!-- UploadView: resumo, sem moldura e menor -->
<CartaoIndicador :rotulo="c.rotulo" :valor="c.valor" :tom="c.tom"
                 tamanho="medio" :caixa="false" />
```

**Por que o `tom` é um nome e não uma classe do Tailwind?** Porque assim a tela
que usa o componente não precisa repetir `text-emerald-600 dark:text-emerald-400`.
A tradução de nome para classe acontece dentro do componente, num só lugar.

## 5.2 Eventos — avisos subindo

Prop desce, evento sobe. O `Header` não sabe abrir a sidebar; ele só avisa:

```js
// Header.vue
defineEmits(['alternar-sidebar'])
```
```html
<button @click="$emit('alternar-sidebar')">☰</button>
```

Quem tem o estado é o `AppLayout`, e é ele quem reage:

```html
<!-- AppLayout.vue -->
<Sidebar :aberta="sidebarAberta" @navegar="fecharNoCelular" />
<Header :sidebar-aberta="sidebarAberta" @alternar-sidebar="sidebarAberta = !sidebarAberta" />
```

Esse é o padrão **"props down, events up"**, e é a pergunta de prova mais
provável sobre componentização.

## 5.3 Valores padrão que servem de exemplo

O `RevenueChart` tem dados de amostra como valor padrão das props:

```js
const props = defineProps({
  titulo: { type: String, default: 'Faturamento por Segmento' },
  rotulos: { type: Array, default: () => ['Varejo', 'Indústria', 'Serviços', 'Saúde', 'Educação'] },
  valores: { type: Array, default: () => [120000, 250000, 180000, 90000, 60000] }
})
```

Por isso a Home usa `<RevenueChart />` sem passar nada e mesmo assim mostra um
gráfico, enquanto a tela de Gráficos passa os dados reais da planilha.

> **Atenção:** props do tipo `Array` e `Object` precisam de `default` como
> **função** (`() => [...]`). Se colocar o array direto, todas as instâncias do
> componente compartilham o mesmo array.

---

# 6. Composables: estado compartilhado

## 6.1 O truque do `ref` fora da função

```js
// useClientes.js
const clientes = ref([])        // FORA da função: existe um só no app inteiro

export function useClientes() {
  const temDados = computed(() => clientes.value.length > 0)
  // ...
  return { clientes, temDados, importarPlanilha }
}
```

Se o `ref` estivesse **dentro** da função, cada componente que chamasse
`useClientes()` ganharia uma lista própria — e a planilha enviada no Upload não
apareceria nos Relatórios. Estando **fora**, o módulo é avaliado uma única vez e
todos compartilham o mesmo estado.

É o mesmo padrão do `useTheme.js`, que já existia no projeto.

## 6.2 `ref` × `computed`

| | `ref` | `computed` |
|---|---|---|
| Para que serve | guardar um valor que muda | derivar um valor de outros |
| Quem altera | seu código (`x.value = ...`) | ninguém: recalcula sozinho |
| No projeto | `clientes`, `busca`, `sidebarAberta` | `indicadores`, `clientesFiltrados` |

Exemplo real, dos Relatórios:

```js
const busca = ref('')                              // o usuário digita aqui
const clientesFiltrados = computed(() => {         // isto se refaz sozinho
  const termo = busca.value.trim().toLowerCase()
  if (!termo) return clientesExibidos.value
  return clientesExibidos.value.filter((c) => c.empresa.toLowerCase().includes(termo))
})
```

## 6.3 Persistência na sessão

O `useClientes` salva o resultado no `sessionStorage`, para um F5 não apagar a
planilha. Dois cuidados que estão no código:

```js
try {
  sessionStorage.setItem(CHAVE, JSON.stringify({ ...lido, nomeArquivo: arquivo.name }))
} catch {
  // planilha grande demais para a sessão: segue só em memória
}
```

E, ao recuperar, as datas precisam ser reconvertidas — **JSON não guarda `Date`**,
elas voltam como texto:

```js
cliente.dataInicio = cliente.dataInicio ? new Date(cliente.dataInicio) : null
```

---

# 7. Lendo o Excel com SheetJS

## 7.1 A biblioteca

```bash
npm install https://cdn.sheetjs.com/xlsx-0.20.3/xlsx-0.20.3.tgz
```

**Por que não `npm install xlsx`?** A versão publicada no npm está parada na
0.18.5 e tem duas vulnerabilidades sem correção (prototype pollution e ReDoS) —
`npm audit` acusa "high severity, no fix available". A SheetJS migrou a
distribuição para o CDN próprio. Instalando de lá, `npm audit` dá zero.

## 7.2 Como o arquivo vira objeto

```js
export async function lerPlanilha(arquivo) {
  const XLSX = await import('xlsx')            // import dinâmico
  const buffer = await arquivo.arrayBuffer()   // o File do navegador vira bytes
  const pasta = XLSX.read(buffer, { type: 'array' })
  // ...
}
```

O `await import('xlsx')` é o que mantém a biblioteca fora do carregamento
inicial do site.

## 7.3 Sobreviver a planilha feita à mão

Três defesas que valem explicar na apresentação:

**1. Achar o cabeçalho.** Planilhas humanas têm título e linhas em branco antes
da tabela. O código lê as linhas cruas e procura a primeira que tenha pelo menos
duas colunas conhecidas.

**2. Aceitar variação de nome de coluna.** `Faixa de Faturamento Anual`,
`faturamento_mensal` e `Faturamento` devem cair no mesmo campo. Cada coluna tem
uma lista de apelidos, comparados depois de normalizar:

```js
function normalizar(texto) {
  return String(texto ?? '')
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '')  // tira acento
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '')                          // tira espaço e pontuação
}
```

**3. Entender número escrito de qualquer jeito.** `R$ 1.200.000,00`, `850 mil`,
`3,5M`, `1 milhão` e faixas como `De R$ 100 mil a R$ 300 mil` (que viram o ponto
médio).

---

# 8. Padronização e validação

Esta é a parte que responde ao problema do cliente, e a que tem critério
objetivo de correção.

## 8.1 A base oficial

`Data/CTI_Insights_Base_Validacao_300_Registros.xlsx` tem 5 abas:

| Aba | Para que serve |
|---|---|
| `dados_importacao` | os 300 registros — a única que deve ser importada |
| `gabarito_erros` | **o gabarito**: 37 erros plantados, com linha, tipo e campo |
| `listas_referencia` | os domínios válidos |
| `resumo` | as contagens esperadas |
| `instrucoes` | manda importar só a primeira aba |

## 8.2 Padronizar ≠ validar

Essa distinção é o coração do tratamento, e é a pergunta mais provável da
professora:

- **Padronizar** é consertar a grafia: `SAUDE`, `Saude` e `saúde` viram `Saúde`.
  Resultado: **27 grafias de segmento viraram as 10 oficiais**, e 17 de serviço
  viraram 8. Sem isso, o gráfico por segmento teria 27 barras em vez de 10.
- **Validar** é apontar o que está errado e não pode ser consertado sozinho:
  código duplicado, faturamento negativo, data que não existe.

O caso que mostra a diferença: o status `ativo` em minúsculo é **acusado como
erro de domínio** (o gabarito exige) **e ainda assim exibido como `ATIVO`** na
tabela. Validação compara o texto exato; padronização compara o texto normalizado.

## 8.3 As cinco regras

| Tipo | Regra | Qtd |
|---|---|---|
| `CAMPO_OBRIGATORIO` | `codigo_cliente`, `cliente`, `consultor` ou `servico` vazio | 10 |
| `DOMINIO_INVALIDO` | `nivel` ou `status_contrato` fora da lista de referência | 10 |
| `VALOR_NEGATIVO` | `faturamento_mensal` ou `valor_servico` menor que zero | 9 |
| `DUPLICIDADE` | `codigo_cliente` repetido — acusa a **segunda** ocorrência | 5 |
| `DATA_INVALIDA` | `data_inicio` que não existe (`31/02/2026`, `2026-13-40`) | 3 |
| | **Total** | **37** |

Detalhe da data: `new Date(2026, 1, 31)` não dá erro, vira 3 de março. Por isso
a validação confere se a data **sobreviveu** à conversão:

```js
const existe =
  data.getUTCFullYear() === Number(ano) &&
  data.getUTCMonth() === Number(mes) - 1 &&
  data.getUTCDate() === Number(dia)
```

## 8.4 O que acontece com a linha errada

Ela **não é descartada**. Aparece marcada com ⚠ nos Relatórios (com o motivo no
balão) e fica **fora dos indicadores** — somar faturamento negativo ou contar
cliente duplicado distorceria a análise.

---

# 9. Tema claro/escuro e responsividade

## 9.1 Cores da marca

No `style.css`, o Tailwind v4 declara as cores da marca com `@theme`, o que cria
as classes `bg-brand`, `text-brand-light` etc.

## 9.2 O tema

O `useTheme` decide a cor em três passos: preferência salva no `localStorage` →
preferência do sistema operacional → aplica a classe `dark` no `<html>`. Todas
as classes `dark:` do Tailwind reagem a essa classe.

## 9.3 Responsividade

O projeto é **mobile-first**: a classe sem prefixo vale para celular, e
`sm:`, `md:` acrescentam a partir daquela largura.

```html
<div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
```

Uma coluna no celular, três a partir de 640 px. E a tabela dos Relatórios fica
dentro de `overflow-x-auto`, para rolar sozinha na horizontal sem quebrar a
página.

---

# 10. Gráfico com Chart.js

O `vue-chartjs` embrulha o Chart.js num componente Vue. É preciso **registrar**
os módulos usados:

```js
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)
```

Esquecer o `register` é o erro mais comum — o gráfico simplesmente não aparece.

As opções e os dados são `computed` que dependem do `isDark`, então **o gráfico
troca de cor junto com o tema**, sem recarregar a página.

---

# 11. Comandos para reproduzir a verificação do gabarito

Para provar que o tratamento está correto, salve o arquivo abaixo como
`FrontEnd/conferir-gabarito.mjs`:

```js
import * as fs from 'node:fs'
import * as XLSX from 'xlsx'
import { lerPlanilha } from './src/utils/planilha.js'
XLSX.set_fs(fs)

const caminho = '../Data/CTI_Insights_Base_Validacao_300_Registros.xlsx'
const bytes = fs.readFileSync(caminho)
const { erros, resumo } = await lerPlanilha({
  name: 'base.xlsx',
  arrayBuffer: async () => bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength)
})

console.log('RESUMO:', JSON.stringify(resumo))

const chave = (e) => `${e.linha ?? e.linha_excel}|${e.tipo ?? e.tipo_erro}|${e.campo}`
const gabarito = XLSX.utils.sheet_to_json(
  XLSX.read(bytes, { type: 'buffer' }).Sheets['gabarito_erros'], { defval: '' }
)

const meus = new Set(erros.map(chave))
const oficiais = new Set(gabarito.map(chave))
console.log('gabarito:', oficiais.size, '| detectados:', meus.size)
console.log('não detectados:', [...oficiais].filter((k) => !meus.has(k)).length)
console.log('falsos positivos:', [...meus].filter((k) => !oficiais.has(k)).length)
```

Rode com:

```bash
cd FrontEnd
node conferir-gabarito.mjs
```

Saída esperada:

```
RESUMO: {"aba":"dados_importacao","linhas":300,"validas":265,"comErro":35,"ocorrencias":37}
gabarito: 37 | detectados: 37
não detectados: 0
falsos positivos: 0
```

Depois de conferir, apague o arquivo — ele é só de verificação e não faz parte
do sistema.

---

# 12. Publicação (pendente)

O back-end será construído em aula e hospedado no **Azure**. Falta definir qual
serviço, e é isso que decide o arquivo que o front precisa:

| Serviço | Arquivo de fallback |
|---|---|
| Static Web Apps | `staticwebapp.config.json` com `navigationFallback` |
| App Service (Windows/IIS) | `web.config` com regra de *rewrite* |
| Storage (site estático) | `index.html` como documento de erro 404 |

**Por que isso é obrigatório:** com `createWebHistory`, abrir
`seusite/app/relatorios` direto na barra de endereço faz o servidor procurar um
arquivo nesse caminho, que não existe, e devolver **404**. O fallback manda
servir o `index.html`, e aí o Vue Router assume.

---

# 13. Git: como o projeto foi versionado

```bash
git status                      # o que mudou
git add -A                      # marca tudo para o commit
git commit -m "feature_nome: descricao"
git push origin main            # envia para o GitHub
git log --oneline -5            # últimos commits
```

Histórico do trabalho:

| Commit | O que entrou |
|---|---|
| `ca62758` | painel interno, sidebar retrátil e rotas `/app` |
| `8dc27ac` | leitura da planilha com SheetJS |
| `41b884b` | padronização e validação da base oficial |
| `bb43e72` | atalhos de `npm` na raiz |
| `bedd04d` | componente `CartaoIndicador` |

---

# 14. Erros comuns e soluções

| Erro | Causa | Solução |
|---|---|---|
| `npm error ENOENT ... package.json` | rodou `npm run dev` na raiz antes dos atalhos existirem | `cd FrontEnd`, ou usar os atalhos da raiz |
| `Port 5173 is in use` | ficou um servidor rodando de antes | fechar o outro terminal, ou aceitar a porta nova que o Vite escolhe |
| `defineProps() cannot reference locally declared variables` | o `validator` usou uma constante do `<script setup>` | escrever os valores literalmente dentro do `defineProps` |
| Gráfico não aparece | faltou `ChartJS.register(...)` | registrar os módulos usados |
| Todas as instâncias do componente com o mesmo array | `default` de prop `Array` sem função | `default: () => [...]` |
| Tela em branco ao acessar rota direto no site publicado | falta o fallback de SPA | criar o arquivo do host (seção 12) |
| Rota `/upload` não encontrada | as rotas do painel mudaram para `/app/...` | usar `/app/upload` |
| Datas viram texto depois do F5 | `JSON.stringify` não guarda `Date` | reconverter com `new Date(...)` ao ler a sessão |
| `npm audit`: high severity em `xlsx` | instalou do npm, que está desatualizado | instalar do CDN da SheetJS (seção 7.1) |
| Gráfico por segmento com barras demais | dados sem padronização | conferir se a aba `listas_referencia` foi lida |
| `Cannot find package 'xlsx'` ao rodar script no Node | o Node resolve pacote pela pasta do script | rodar o script de dentro de `FrontEnd/` |

---

# 15. Perguntas prováveis na apresentação

**O que é um composable e por que usar?** É uma função que agrupa estado e
lógica reaproveitáveis fora dos componentes. Aqui, `useClientes` permite que
Upload, Relatórios e Gráficos enxerguem a mesma planilha sem passar props por
toda a árvore.

**Por que dois layouts?** Porque site público e painel interno têm navegação
diferente. Rotas aninhadas deixam cada grupo com sua moldura, sem `v-if` de
layout espalhado pelas telas.

**Como as telas conversam?** Props descem, eventos sobem, e o que precisa ser
visto por telas distantes fica num composable.

**Qual a diferença entre padronizar e validar?** Seção 8.2 — e o exemplo do
`ativo` minúsculo.

**Por que a linha com erro não é apagada?** Porque o cliente precisa saber o que
está errado na planilha dele. Apagar em silêncio esconderia o problema que o
sistema deveria justamente revelar.

**Como você sabe que o tratamento está certo?** A planilha traz um gabarito com
37 erros; o sistema encontra os 37, sem falso positivo (seção 11).

---

# 16. Checklist de entrega

- [ ] `npm run build` roda sem erro
- [ ] As 5 rotas abrem e navegam entre si
- [ ] O menu hambúrguer funciona no celular (site público)
- [ ] A sidebar recolhe e expande no painel
- [ ] O botão de tema troca claro/escuro e a escolha sobrevive ao F5
- [ ] O upload da base oficial mostra 300 / 265 / 35 / 37
- [ ] Os Relatórios marcam as 35 linhas com erro e o filtro funciona
- [ ] O gráfico mostra 10 segmentos, não 27
- [ ] Todos os commits enviados (`git status` limpo, `main` em dia com `origin/main`)
- [ ] Capturas das telas importadas no Figma
- [ ] Arquivo de fallback de SPA criado, depois que o serviço do Azure for definido
- [ ] Nome e RA preenchidos neste documento
