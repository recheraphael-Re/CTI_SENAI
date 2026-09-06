<template>
  <!-- FOOTER — flex-col no celular, flex-row no computador -->
  <footer class="bg-slate-900 dark:bg-slate-950 text-slate-400 mt-10 border-t border-slate-800">
    <div class="mx-auto flex max-w-6xl flex-col justify-between items-center gap-4 px-6 py-8 sm:flex-row">
      <p class="text-sm text-center sm:text-left">
        © 2026 Projeto Integrador 2/2026 —
        <span class="text-brand-light font-semibold">CTI Comunicação de Dados</span>
      </p>

      <div class="flex gap-6 text-sm font-semibold">
        <RouterLink to="/" class="hover:text-brand-light transition">Início</RouterLink>
        <RouterLink to="/login" class="hover:text-brand-light transition">Login</RouterLink>
        <RouterLink to="/app" class="text-brand-light hover:text-brand-light transition">Painel</RouterLink>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>
