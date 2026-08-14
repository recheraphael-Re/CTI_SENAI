<template>
  <div class="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl p-6 shadow-sm transition-colors duration-300">
    <h3 class="text-xl font-bold text-slate-900 dark:text-slate-100 mb-4">
      Faturamento por Segmento
    </h3>
    <Bar :data="chartData" :options="chartOptions" :key="themeKey" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { useTheme } from '../composables/useTheme.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const { isDark } = useTheme()

// Força re-render quando o tema muda
const themeKey = computed(() => (isDark.value ? 'dark' : 'light'))

// Cores que mudam conforme o tema
const chartData = computed(() => ({
  labels: ['Varejo', 'Indústria', 'Serviços', 'Saúde', 'Educação'],
  datasets: [
    {
      label: 'Faturamento (R$ mil)',
      data: [120, 250, 180, 90, 60],
      backgroundColor: isDark.value
        ? [
            'rgba(139, 92, 246, 0.9)',   // violet-500
            'rgba(129, 140, 248, 0.9)',  // indigo-400
            'rgba(99, 102, 241, 0.9)',   // indigo-500
            'rgba(34, 211, 238, 0.9)',   // cyan-400
            'rgba(167, 139, 250, 0.9)'   // violet-400
          ]
        : [
            'rgba(124, 58, 237, 0.85)',
            'rgba(99, 102, 241, 0.85)',
            'rgba(79, 70, 229, 0.85)',
            'rgba(6, 182, 212, 0.85)',
            'rgba(139, 92, 246, 0.85)'
          ],
      borderRadius: 8,
      borderSkipped: false
    }
  ]
}))

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: { display: false },
    title: { display: false },
    tooltip: {
      backgroundColor: isDark.value ? '#1e293b' : '#ffffff',
      titleColor: isDark.value ? '#f1f5f9' : '#0f172a',
      bodyColor: isDark.value ? '#cbd5e1' : '#475569',
      borderColor: isDark.value ? '#334155' : '#e2e8f0',
      borderWidth: 1
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: { color: isDark.value ? 'rgba(51, 65, 85, 0.5)' : 'rgba(226, 232, 240, 0.5)' },
      ticks: {
        color: isDark.value ? '#94a3b8' : '#475569',
        callback: (value) => `R$ ${value}k`
      }
    },
    x: {
      grid: { display: false },
      ticks: { color: isDark.value ? '#94a3b8' : '#475569' }
    }
  }
}))
</script>
