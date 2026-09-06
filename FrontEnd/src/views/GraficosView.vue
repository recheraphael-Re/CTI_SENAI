<template>
  <!-- GRÁFICOS — KPIs e indicadores da carteira -->
  <section>
    <p class="text-slate-600 dark:text-slate-400 mb-6">
      Visão geral dos principais indicadores e insights.
    </p>

    <!-- Deixa claro quando a tela ainda não tem planilha de verdade -->
    <p
      v-if="usandoExemplo"
      class="mb-6 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-400"
    >
      Estes são dados de exemplo.
      <RouterLink to="/app/upload" class="font-semibold underline">Envie uma planilha</RouterLink>
      para ver a sua carteira.
    </p>
    <p v-else class="mb-6 text-sm text-slate-500 dark:text-slate-400">
      Fonte: <strong class="font-semibold">{{ nomeArquivo }}</strong> —
      {{ resumo.validas }} linhas válidas somam
      {{ formatarMoeda(faturamentoTotal) }} por mês.
      <template v-if="resumo.comErro">
        {{ resumo.comErro }} linhas ficaram de fora por erro de validação.
      </template>
    </p>

    <!-- Cards de indicadores -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
      <CartaoIndicador
        v-for="kpi in indicadores"
        :key="kpi.rotulo"
        :rotulo="kpi.rotulo"
        :valor="kpi.valor"
      />
    </div>

    <!-- Gráfico de faturamento, somado por segmento -->
    <div class="mt-8">
      <RevenueChart
        titulo="Faturamento mensal por segmento"
        :rotulos="faturamentoPorSegmento.rotulos"
        :valores="faturamentoPorSegmento.valores"
      />
    </div>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import RevenueChart from '../components/RevenueChart.vue'
import CartaoIndicador from '../components/CartaoIndicador.vue'
import { useClientes } from '../composables/useClientes.js'
import { formatarMoeda } from '../utils/formato.js'

// Os indicadores saem da planilha enviada no Upload; sem planilha, da amostra
const { indicadores, faturamentoPorSegmento, faturamentoTotal, usandoExemplo, nomeArquivo, resumo } =
  useClientes()
</script>
