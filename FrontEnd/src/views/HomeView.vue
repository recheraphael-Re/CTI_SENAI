<script setup>
import { useAuthStore } from '../stores/auth.js'
import hero from '../img/home.png'

const auth = useAuthStore()
const etapas = [
  { titulo: 'Importe a planilha', descricao: 'Escolha um arquivo XLSX, XLS ou CSV da carteira de clientes.' },
  { titulo: 'Revise as linhas', descricao: 'Confira os erros por registro e confirme o envio das linhas válidas.' },
  { titulo: 'Processe o lote', descricao: 'O Java revalida os dados. O Python padroniza os registros e salva os indicadores no PostgreSQL.' },
  { titulo: 'Consulte o dashboard', descricao: 'Compare segmentos, níveis e consultores e abra os resultados por lote.' }
]
</script>

<template>
  <section class="grid items-center gap-8 border-b border-slate-200 pb-10 pt-3 md:grid-cols-2 md:gap-12 md:py-12 dark:border-slate-800">
    <div>
      <h1 class="text-3xl font-bold tracking-tight sm:text-4xl lg:text-5xl">
        Planilhas de clientes, <span class="text-brand dark:text-brand-light">validação e análise</span>
      </h1>
      <p class="mt-4 max-w-lg text-base leading-relaxed text-slate-600 sm:text-lg dark:text-slate-300">
        Importe os dados comerciais da Provedor CTI, revise os registros da planilha e acompanhe os indicadores da carteira.
      </p>
      <div class="mt-6 flex flex-wrap gap-3">
        <RouterLink
          :to="{ name: auth.logado ? 'upload' : 'login' }"
          class="rounded-lg bg-linear-to-r from-brand to-brand-accent px-5 py-3 font-semibold text-white hover:opacity-90"
        >
          {{ auth.logado ? 'Enviar planilha' : 'Entrar' }}
        </RouterLink>
        <RouterLink
          v-if="!auth.logado"
          :to="{ name: 'cadastro' }"
          class="rounded-lg border border-brand/45 px-5 py-3 font-semibold text-brand hover:bg-brand/10 dark:text-brand-light"
        >
          Criar conta
        </RouterLink>
      </div>
    </div>
    <img :src="hero" alt="Ilustração de uma planilha conectada a um painel de indicadores"
      width="1448" height="1086" class="w-full rounded-2xl border border-brand/15" />
  </section>

  <section class="py-8" aria-labelledby="fluxo">
    <h2 id="fluxo" class="text-xl font-bold text-brand dark:text-brand-light">Da planilha ao dashboard</h2>
    <ol class="mt-4 divide-y divide-slate-200 border-y border-slate-200 dark:divide-slate-800 dark:border-slate-800">
      <li v-for="etapa in etapas" :key="etapa.titulo" class="grid gap-x-6 gap-y-1 py-4 sm:grid-cols-[13rem_1fr]">
        <strong>{{ etapa.titulo }}</strong>
        <span class="text-slate-600 dark:text-slate-300">{{ etapa.descricao }}</span>
      </li>
    </ol>
  </section>
</template>
