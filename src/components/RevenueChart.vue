<template>
  <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
    <h3 class="text-xl font-bold text-slate-900 mb-4">
      Faturamento por Segmento
    </h3>
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
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

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
)

// Gradiente violet → índigo aplicado nas barras
const chartData = {
  labels: ['Varejo', 'Indústria', 'Serviços', 'Saúde', 'Educação'],
  datasets: [
    {
      label: 'Faturamento (R$ mil)',
      data: [120, 250, 180, 90, 60],
      backgroundColor: [
        'rgba(124, 58, 237, 0.85)',  // violet-600
        'rgba(99, 102, 241, 0.85)',  // indigo-500
        'rgba(79, 70, 229, 0.85)',   // indigo-600
        'rgba(6, 182, 212, 0.85)',   // cyan-500
        'rgba(139, 92, 246, 0.85)'   // violet-500
      ],
      borderRadius: 8,
      borderSkipped: false
    }
  ]
}

const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: { display: false },
    title: { display: false }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: { color: 'rgba(226, 232, 240, 0.5)' },
      ticks: {
        color: '#475569',
        callback: (value) => `R$ ${value}k`
      }
    },
    x: {
      grid: { display: false },
      ticks: { color: '#475569' }
    }
  }
}
</script>
