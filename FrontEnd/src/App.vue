<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <AppHeader />
    <main id="conteudo" class="mx-auto w-full min-w-0 max-w-6xl flex-1 px-4 py-8 sm:px-6 lg:px-8">
      <RouterView />
    </main>
    <footer class="mt-8 border-t border-slate-200 py-5 text-center text-sm text-slate-600 dark:border-slate-800 dark:text-slate-400">
      CTI Insights · Projeto Integrador SENAI
    </footer>
  </div>
</template>
