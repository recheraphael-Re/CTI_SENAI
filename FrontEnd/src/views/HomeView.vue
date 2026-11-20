<script setup>
import { useAuthStore } from '../stores/auth.js'
import hero from '../img/home.png'

const auth = useAuthStore()
const etapas = [
  { icone: '📤', titulo: 'Importe a planilha', descricao: 'Escolha um arquivo XLSX, XLS ou CSV da carteira de clientes.' },
  { icone: '📋', titulo: 'Revise as linhas', descricao: 'Confira os erros por registro e confirme o envio das linhas válidas.' },
  { icone: '⚙️', titulo: 'Processe o lote', descricao: 'O Java revalida os dados. O Python padroniza os registros e salva os indicadores no PostgreSQL.' },
  { icone: '📈', titulo: 'Consulte o dashboard', descricao: 'Compare segmentos, níveis e consultores e abra os resultados por lote.' }
]
</script>

<template>
  <section class="mx-auto max-w-3xl py-8 text-center md:py-14">
    <h1 class="text-3xl font-bold leading-tight tracking-tight sm:text-4xl lg:text-5xl">
      Planilhas de clientes,
      <span class="bg-linear-to-r from-brand to-brand-accent bg-clip-text text-transparent dark:from-brand-light dark:to-brand-accent-light">validação e análise</span>
    </h1>
    <p class="mx-auto mt-4 max-w-xl text-base leading-relaxed text-slate-600 sm:text-lg dark:text-slate-300">
      Importe os dados comerciais da Provedor CTI, revise os registros da planilha e acompanhe os indicadores da carteira.
    </p>
    <div class="mt-6 flex flex-wrap justify-center gap-3">
      <RouterLink
        :to="{ name: auth.logado ? 'upload' : 'login' }"
        class="rounded-lg bg-linear-to-r from-brand to-brand-accent px-5 py-3 font-semibold text-white shadow-lg shadow-brand/25 hover:opacity-90"
      >
        {{ auth.logado ? 'Enviar planilha' : 'Entrar' }}
      </RouterLink>
      <RouterLink
        v-if="!auth.logado"
        :to="{ name: 'cadastro' }"
        class="rounded-lg border border-brand/45 bg-white px-5 py-3 font-semibold text-brand hover:bg-brand/10 dark:bg-slate-900 dark:text-brand-light dark:hover:bg-brand/10"
      >
        Criar conta
      </RouterLink>
    </div>
  </section>

  <section aria-labelledby="fluxo">
    <h2 id="fluxo" class="text-xl font-bold text-brand dark:text-brand-light">Da planilha ao dashboard</h2>
    <ol class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <li v-for="etapa in etapas" :key="etapa.titulo" class="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
        <span aria-hidden="true" class="grid size-11 place-items-center rounded-lg bg-linear-to-br from-brand to-brand-accent text-xl">{{ etapa.icone }}</span>
        <h3 class="mt-4 font-bold">{{ etapa.titulo }}</h3>
        <p class="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">{{ etapa.descricao }}</p>
      </li>
    </ol>
  </section>
  <div class="relative mx-auto mt-12 max-w-2xl pl-3 pt-3">
    <div aria-hidden="true" class="absolute left-0 top-0 size-20 rounded-lg bg-brand"></div>
    <img :src="hero" alt="Ilustração de uma planilha conectada a um painel de indicadores"
      width="1448" height="1086" class="relative w-full rounded-xl border border-brand/15 shadow-xl" />
  </div>
</template>
