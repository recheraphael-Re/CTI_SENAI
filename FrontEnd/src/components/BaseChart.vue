<template>
  <section class="min-w-0 rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-800">
    <h3 class="mb-3 text-lg font-bold">{{ titulo }}</h3>
    <p v-if="descricao" class="mb-3 text-sm text-slate-500 dark:text-slate-400">{{ descricao }}</p>
    <div v-if="dados.rotulos.length" class="h-72">
      <component :is="linha ? Line : Bar" :data="chartData" :options="options" :aria-label="titulo" role="img" />
    </div>
    <p v-else class="py-10 text-sm">Sem dados suficientes para este gráfico.</p>
    <details v-if="dados.rotulos.length" class="mt-3 text-sm">
      <summary class="cursor-pointer">Ver valores do gráfico</summary>
      <ul class="mt-2"><li v-for="(nome, i) in dados.rotulos" :key="nome">{{ nome }}: {{ formatar(dados.valores[i]) }}</li></ul>
    </details>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { Bar, Line } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, PointElement, LineElement, Tooltip, Legend } from 'chart.js'
import { useTheme } from '../composables/useTheme.js'
import { formatarMoeda } from '../utils/formato.js'
ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, Tooltip, Legend)
const props = defineProps({
  titulo: String,
  descricao: String,
  dados: { type: Object, required: true },
  linha: Boolean,
  moeda: Boolean
})
const { isDark } = useTheme()
const cor = computed(() => isDark.value ? '#a78bfa' : '#7c3aed')
const paleta = computed(() => isDark.value
  ? ['#a78bfa', '#818cf8', '#6366f1', '#c4b5fd', '#8b5cf6']
  : ['#7c3aed', '#6366f1', '#4f46e5', '#a78bfa', '#8b5cf6'])
const formatar = valor => props.moeda ? formatarMoeda(valor) : valor.toLocaleString('pt-BR')
const chartData = computed(() => ({
  labels: props.dados.rotulos,
  datasets: [{
    label: props.moeda ? 'Faturamento' : 'Quantidade',
    data: props.dados.valores,
    backgroundColor: props.moeda
      ? props.dados.valores.map((_, i) => paleta.value[i % paleta.value.length])
      : cor.value,
    borderColor: cor.value,
    borderRadius: 6,
    borderSkipped: false,
    tension: 0.25
  }]
}))
const options = computed(() => {
  const corRotulo = isDark.value ? '#cbd5e1' : '#475569'
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: { callbacks: { label: contexto => formatar(contexto.parsed.y) } }
    },
    scales: {
      x: { grid: { display: false }, ticks: { color: corRotulo } },
      y: {
        beginAtZero: true,
        grid: { color: isDark.value ? '#334155' : '#ede9fe' },
        ticks: props.moeda ? { color: corRotulo, callback: formatar } : { color: corRotulo, precision: 0 }
      }
    }
  }
})
</script>
