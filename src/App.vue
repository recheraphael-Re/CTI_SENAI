<template>
  <!-- ====== CABEÇALHO ====== -->
  <header class="bg-white border-b border-slate-200 sticky top-0 z-50">
    <div class="max-w-6xl mx-auto flex flex-col md:flex-row justify-between items-center gap-4 p-4">
      <!-- Logo com gradiente -->
      <h1 class="text-xl md:text-2xl font-bold bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent">
        CTI Dados
      </h1>

      <!-- Menu -->
      <nav class="flex flex-wrap justify-center gap-4 md:gap-6 text-slate-700 font-medium">
        <a href="#" class="hover:text-violet-600 transition">Início</a>
        <a href="#" class="hover:text-violet-600 transition">Sobre</a>
        <a href="#" class="hover:text-violet-600 transition">Serviços</a>
        <a href="#" class="hover:text-violet-600 transition">Contato</a>
      </nav>
    </div>
  </header>

  <!-- ====== CONTEÚDO PRINCIPAL ====== -->
  <main class="max-w-6xl mx-auto p-6 md:p-10">
    <!-- Hero -->
    <section class="py-10 md:py-16 text-center">
      <!-- Badge -->
      <span class="inline-block bg-violet-100 text-violet-700 text-xs font-semibold px-3 py-1 rounded-full mb-4">
        🚀 Projeto Integrador 2026
      </span>

      <h2 class="text-3xl md:text-5xl font-bold text-slate-900 mb-4 leading-tight">
        Transforme sua planilha em
        <span class="bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent">
          dados estratégicos
        </span>
      </h2>

      <p class="text-slate-600 max-w-2xl mx-auto mb-6 text-lg">
        Organize, analise e gere insights da sua carteira de clientes com base em dados reais.
      </p>

      <!-- Botões CTA -->
      <div class="flex flex-col sm:flex-row gap-3 justify-center">
        <button class="bg-gradient-to-r from-violet-600 to-indigo-600 text-white px-6 py-3 rounded-lg hover:opacity-90 transition shadow-lg hover:shadow-violet-500/30 font-semibold">
          Começar agora
        </button>
        <button class="bg-white border border-slate-300 text-slate-700 px-6 py-3 rounded-lg hover:border-violet-400 hover:text-violet-600 transition font-semibold">
          Saiba mais
        </button>
      </div>
    </section>

    <!-- Grid de serviços -->
    <section class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-10">
      <ServiceCard
        v-for="servico in servicos"
        :key="servico.titulo"
        :titulo="servico.titulo"
        :descricao="servico.descricao"
        :icone="servico.icone"
      />
    </section>

    <!-- Gráfico -->
    <section class="mt-10">
      <RevenueChart />
    </section>
  </main>

  <!-- ====== RODAPÉ ====== -->
  <footer class="bg-slate-900 text-slate-400 text-center p-6 mt-10">
    <p class="text-sm">
      © 2026 Projeto Integrador 2/2026 —
      <span class="text-violet-400 font-semibold">CTI Comunicação de Dados</span>
    </p>
  </footer>
</template>

<script setup>
import { ref } from 'vue'
import ServiceCard from './components/ServiceCard.vue'
import RevenueChart from './components/RevenueChart.vue'

const servicos = ref([
  {
    titulo: 'Limpeza de Dados',
    descricao: 'Padronização e tratamento de planilhas Excel para análise confiável.',
    icone: '🧹'
  },
  {
    titulo: 'Análise Estratégica',
    descricao: 'Identificação de padrões e oportunidades comerciais na carteira.',
    icone: '📈'
  },
  {
    titulo: 'Dashboards',
    descricao: 'Visualização clara de faturamento, segmentos e perfil de clientes.',
    icone: '📊'
  }
])
</script>
