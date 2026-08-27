<template>
  <!-- DASHBOARD — KPIs, gráficos e visão geral da carteira -->
  <section class="py-10">
    <h2 class="text-2xl md:text-3xl font-bold text-slate-900 dark:text-slate-100">
      Dashboard
    </h2>
    <p class="text-slate-600 dark:text-slate-400 mt-1 mb-6">
      Visão geral dos principais indicadores e insights.
    </p>

    <!-- Cards de indicadores -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
      <div
        v-for="kpi in indicadores"
        :key="kpi.rotulo"
        class="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 text-center"
      >
        <p class="text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">
          {{ kpi.rotulo }}
        </p>
        <p class="mt-2 text-3xl font-bold bg-gradient-to-r from-brand to-brand-accent dark:from-brand-light dark:to-brand-accent-light bg-clip-text text-transparent">
          {{ kpi.valor }}
        </p>
      </div>
    </div>

    <!-- Gráfico de faturamento -->
    <div class="mt-8">
      <GraficoFaturamento />
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import GraficoFaturamento from '../components/GraficoFaturamento.vue'

// Dados de exemplo — serão substituídos pelos dados da planilha tratada
const indicadores = ref([
  { rotulo: 'Total de clientes', valor: '1.250' },
  { rotulo: 'Faturamento médio', valor: 'R$ 450k' },
  { rotulo: 'Serviços ativos', valor: '3.420' }
])
</script>
