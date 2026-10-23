<template>
  <!-- SIDEBAR — menu lateral retrátil do painel interno.
       No desktop encolhe para só os ícones; no celular desliza por cima. -->
  <aside
    class="fixed inset-y-0 left-0 z-40 flex flex-col border-r border-slate-200 bg-white transition-all duration-300 dark:border-slate-800 dark:bg-slate-900"
    :class="[
      aberta ? 'w-60' : 'w-0 md:w-16',
      aberta ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
    ]"
  >
    <!-- Logo do painel -->
    <div class="flex h-16 items-center gap-2 overflow-hidden px-4">
      <span class="text-xl">📊</span>
      <span
        v-show="aberta"
        class="truncate bg-gradient-to-r from-brand to-brand-accent bg-clip-text font-bold text-transparent dark:from-brand-light dark:to-brand-accent-light"
      >
        CTI Insights
      </span>
    </div>

    <!-- Links das telas internas -->
    <nav class="flex flex-1 flex-col gap-1 overflow-hidden px-2 py-4">
      <RouterLink
        v-for="item in itensVisiveis"
        :key="item.to"
        :to="item.to"
        class="flex items-center gap-3 rounded-lg px-3 py-2 text-slate-700 transition hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
        active-class="bg-brand/10 text-brand dark:text-brand-light"
        :title="item.rotulo"
        @click="$emit('navegar')"
      >
        <span class="shrink-0 text-lg">{{ item.icone }}</span>
        <span v-show="aberta" class="truncate font-medium">{{ item.rotulo }}</span>
      </RouterLink>
    </nav>

    <!-- Voltar ao site público -->
    <div class="overflow-hidden border-t border-slate-200 p-2 dark:border-slate-800">
      <button type="button" class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800" @click="sair">
        <span aria-hidden="true">↩</span><span v-show="aberta">{{ modoDemonstracao ? 'Voltar ao site' : 'Sair' }}</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { modoDemonstracao } from '../utils/modoAcesso.js'
import { ref, computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

// Propriedade: o layout controla se a barra está aberta ou recolhida
defineProps({
  aberta: { type: Boolean, default: true }
})

// Evento para o layout fechar a barra no celular após escolher uma tela
defineEmits(['navegar'])

const auth = useAuthStore()
const router = useRouter()
function sair() {
  if (modoDemonstracao) { router.replace('/'); return }
  auth.logout()
  router.replace('/login')
}
const itens = ref([
  { rotulo: 'Usuários', to: '/app/usuarios', icone: '👥', admin: true },
  { rotulo: 'Upload', to: '/app/upload', icone: '📤' },
  { rotulo: 'Relatórios', to: '/app/relatorios', icone: '📋' },
  { rotulo: 'Gráficos', to: '/app/graficos', icone: '📈' }
])
const itensVisiveis = computed(() => itens.value.filter(item => !item.admin || (!modoDemonstracao && auth.isAdmin)))
</script>
