# CTI Insights — Projeto Integrador 2/2026

Landing page do projeto **CTI Comunicação de Dados**, do curso Técnico em
Comunicação de Dados (SENAI). A página apresenta os serviços de tratamento e
análise de carteiras de clientes e exibe um gráfico de faturamento por segmento.

## Tecnologias

- [Vue 3](https://vuejs.org/) (Composition API, `<script setup>`)
- [Vite](https://vite.dev/) — servidor de desenvolvimento e build
- [Tailwind CSS v4](https://tailwindcss.com/) — estilização
- [Chart.js](https://www.chartjs.org/) + [vue-chartjs](https://vue-chart-3.netlify.app/) — gráficos

## Como rodar

```bash
npm install     # instala as dependências
npm run dev     # inicia em http://localhost:5173
npm run build   # gera a versão de produção em dist/
npm run preview # visualiza o build de produção
```

## Estrutura

```
src/
├── App.vue                    # página principal (header, hero, serviços, rodapé)
├── main.js                    # ponto de entrada
├── style.css                  # Tailwind + estilos base e dark mode
├── components/
│   ├── ServiceCard.vue        # card de serviço (props: titulo, descricao, icone)
│   ├── RevenueChart.vue       # gráfico de barras de faturamento
│   └── ThemeToggle.vue        # botão de alternância claro/escuro
└── composables/
    └── useTheme.js            # estado do tema (localStorage + preferência do sistema)
```

## Tema claro/escuro

O tema é controlado pela classe `.dark` no elemento `<html>`, aplicada pelo
composable `useTheme`. A preferência é salva em `localStorage`; na primeira
visita, o sistema operacional do usuário define o padrão.

No Tailwind v4 a variante `dark:` usa `prefers-color-scheme` por padrão, então o
modo por classe é habilitado em `src/style.css`:

```css
@custom-variant dark (&:where(.dark, .dark *));
```

## Dados

Os valores do gráfico em `RevenueChart.vue` são de exemplo e estão fixos no
código. Para usar dados reais, substitua o array `data` pelos números extraídos
da planilha da carteira de clientes.
