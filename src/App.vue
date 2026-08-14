<template>
  <!-- ====== CABEÇALHO ====== -->
  <header class="flex flex-col md:flex-row justify-between items-center gap-4 bg-white p-4 shadow-sm">
    <!-- Layout: flex-col no mobile, flex-row no desktop -->

    <!-- Logo -->
    <h1 class="text-xl md:text-2xl font-bold text-red-600">
      CTI Dados
    </h1>

    <!-- Menu (dentro do header, semanticamente correto) -->
    <nav class="flex flex-wrap justify-center gap-4 md:gap-6 text-red-600 font-medium">
      <a href="#" class="hover:text-red-800 transition cursor-pointer">Início</a>
      <a href="#" class="hover:text-red-800 transition cursor-pointer">Sobre</a>
      <a href="#" class="hover:text-red-800 transition cursor-pointer">Serviços</a>
      <a href="#" class="hover:text-red-800 transition cursor-pointer">Contato</a>
    </nav>
  </header>

  <!-- ====== CONTEÚDO PRINCIPAL ====== -->
  <main class="p-6 md:p-10 text-center">
    <!-- Hero -->
    <section class="py-10 md:py-16">
      <h2 class="text-3xl md:text-4xl font-bold text-black mb-4">
        Transforme sua planilha em dados estratégicos
      </h2>
      <p class="text-gray-600 max-w-2xl mx-auto mb-6">
        Organize, analise e gere insights da sua carteira de clientes com base em dados reais.
      </p>
      <button class="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition cursor-pointer shadow-md">
        Começar agora
      </button>
    </section>

    <!-- Grid de serviços (aproveitando o tema do PI) -->
    <section class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-10">
      <!-- Card 1 -->
      <div class="bg-white border-2 border-gray-200 rounded-lg p-6 hover:border-red-600 transition cursor-pointer shadow-sm">
        <h3 class="text-xl font-bold text-black mb-2">Limpeza de Dados</h3>
        <p class="text-gray-600">Padronização e tratamento de planilhas Excel.</p>
      </div>

      <!-- Card 2 -->
      <div class="bg-white border-2 border-gray-200 rounded-lg p-6 hover:border-red-600 transition cursor-pointer shadow-sm">
        <h3 class="text-xl font-bold text-black mb-2">Análise Estratégica</h3>
        <p class="text-gray-600">Identificação de padrões e oportunidades comerciais.</p>
      </div>

      <!-- Card 3 -->
      <div class="bg-white border-2 border-gray-200 rounded-lg p-6 hover:border-red-600 transition cursor-pointer shadow-sm">
        <h3 class="text-xl font-bold text-black mb-2">Dashboards</h3>
        <p class="text-gray-600">Visualização clara de faturamento e perfil de clientes.</p>
      </div>
    </section>
  </main>

  <!-- ====== RODAPÉ ====== -->
  <footer class="bg-black text-white text-center p-4 mt-10">
    © 2026 Projeto Integrador 2/2026 — CTI Comunicação de Dados
  </footer>
</template>

<script setup>
// Aqui entra a lógica do seu site (funções, dados reativos, etc.)
</script>
