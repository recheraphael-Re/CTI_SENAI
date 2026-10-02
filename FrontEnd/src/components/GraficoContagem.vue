<template>
  <section class="min-w-0 rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-800">
    <h3 class="mb-3 text-lg font-bold">{{ titulo }}</h3>
    <p class="mb-3 text-sm text-slate-500 dark:text-slate-400">{{ descricao }}</p>
    <div v-if="dados.rotulos.length" class="h-72">
      <component :is="linha ? Line : Bar" :data="chartData" :options="options" :aria-label="titulo" role="img" />
    </div>
    <p v-else class="py-10 text-sm">Sem dados suficientes para este gráfico.</p>
    <details v-if="dados.rotulos.length" class="mt-3 text-sm">
      <summary class="cursor-pointer">Ver valores do gráfico</summary>
      <ul class="mt-2"><li v-for="(nome, i) in dados.rotulos" :key="nome">{{ nome }}: {{ dados.valores[i] }}</li></ul>
    </details>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { Bar, Line } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, PointElement, LineElement, Tooltip, Legend } from 'chart.js'
import { useTheme } from '../composables/useTheme.js'
ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, Tooltip, Legend)
const props = defineProps({ titulo: String, descricao: String, dados: { type: Object, required: true }, linha: Boolean })
const { isDark } = useTheme()
const chartData = computed(() => ({ labels: props.dados.rotulos, datasets: [{ label: 'Quantidade', data: props.dados.valores, backgroundColor: '#8b5cf6', borderColor: '#8b5cf6', tension: 0 }] }))
const options = computed(() => ({
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { ticks: { color: isDark.value ? '#cbd5e1' : '#475569' } },
    y: { beginAtZero: true, ticks: { precision: 0, color: isDark.value ? '#cbd5e1' : '#475569' } }
  }
}))
</script>
