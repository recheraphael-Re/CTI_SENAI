<template>
  <!-- HEADER — logo à esquerda; menu, tema e hambúrguer à direita -->
  <header class="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 sticky top-0 z-50 transition-colors duration-300">
    <div class="max-w-6xl mx-auto flex justify-between items-center gap-4 p-4">
      <h1 class="text-xl md:text-2xl font-bold bg-gradient-to-r from-brand to-brand-accent dark:from-brand-light dark:to-brand-accent-light bg-clip-text text-transparent">
        CTI Insights
      </h1>

      <div class="flex items-center gap-3 md:gap-6">
        <!-- hidden md:flex: o menu em linha só aparece do tablet para cima -->
        <NavMenu class="hidden md:flex" />

        <ThemeToggle />

        <!-- md:hidden: o botão hambúrguer só existe no celular -->
        <button
          type="button"
          class="md:hidden rounded-lg p-2 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          :aria-expanded="aberto"
          aria-controls="menu-celular"
          :aria-label="aberto ? 'Fechar menu' : 'Abrir menu'"
          @click="aberto = !aberto"
        >
          <!-- Ícone muda conforme o estado: ☰ fechado, ✕ aberto -->
          <svg class="h-6 w-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path
              v-if="!aberto"
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M4 6h16M4 12h16M4 18h16"
            />
            <path
              v-else
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- Painel que abre no celular — v-if controlado pelo estado `aberto` -->
    <div
      v-if="aberto"
      id="menu-celular"
      class="md:hidden border-t border-slate-200 dark:border-slate-800 px-4 py-3"
    >
      <!-- vertical: empilha os links | @navegar: fecha ao escolher uma tela -->
      <NavMenu vertical @navegar="aberto = false" />
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import NavMenu from './NavMenu.vue'
import ThemeToggle from './ThemeToggle.vue'

// ESTADO: o menu do celular está aberto?
const aberto = ref(false)
</script>
