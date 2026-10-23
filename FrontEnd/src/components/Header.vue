<template>
  <!-- HEADER — cabeçalho fixo do painel: hambúrguer, título e usuário -->
  <header class="sticky top-0 z-30 flex h-16 items-center justify-between gap-4 border-b border-slate-200 bg-white px-4 transition-colors duration-300 dark:border-slate-800 dark:bg-slate-900">
    <div class="flex items-center gap-3">
      <!-- Botão hambúrguer: abre e fecha a Sidebar -->
      <button
        type="button"
        class="rounded-lg p-2 text-slate-700 transition hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
        :aria-label="sidebarAberta ? 'Recolher menu' : 'Expandir menu'"
        :aria-expanded="sidebarAberta"
        @click="$emit('alternar-sidebar')"
      >
        <svg class="h-6 w-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      <!-- Nome da tela atual, vindo do próprio roteador -->
      <h2 class="text-lg font-bold text-slate-900 dark:text-slate-100">
        {{ tituloDaTela }}
      </h2>
    </div>

    <div class="flex items-center gap-3">
      <ThemeToggle />

      <!-- Informações do usuário -->
      <div class="flex items-center gap-2">
        <span class="hidden text-sm font-medium text-slate-600 sm:block dark:text-slate-400">
          {{ modoDemonstracao ? 'Demonstração' : auth.usuario?.nome }}
        </span>
        <span class="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-brand to-brand-accent font-bold text-white">
          {{ modoDemonstracao ? 'D' : auth.usuario?.nome?.charAt(0).toUpperCase() }}
        </span>
      </div>
    </div>
  </header>
</template>

<script setup>
import { modoDemonstracao } from '../utils/modoAcesso.js'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import ThemeToggle from './ThemeToggle.vue'

const auth = useAuthStore()

defineProps({
  sidebarAberta: { type: Boolean, default: true }
})

defineEmits(['alternar-sidebar'])

// O título acompanha a rota ativa, sem precisar repetir texto em cada tela
const route = useRoute()
const titulos = {
  usuarios: 'Administração de usuários',
  upload: 'Upload de Planilha',
  relatorios: 'Relatórios',
  graficos: 'Gráficos e Indicadores'
}
const tituloDaTela = computed(() => titulos[route.name] ?? 'Painel')
</script>
