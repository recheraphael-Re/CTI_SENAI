<template>
  <!-- NAVMENU — navegação do site público.
       A direção vem por PROPRIEDADE, então não há código duplicado. -->
  <nav
    class="font-medium text-slate-700 dark:text-slate-300"
    :class="vertical ? 'flex flex-col gap-1' : 'flex items-center gap-4 md:gap-6'"
  >
    <RouterLink
      v-for="item in itens"
      :key="item.to"
      :to="item.to"
      class="transition hover:text-brand dark:hover:text-brand-light"
      :class="vertical ? 'rounded-lg px-3 py-2 hover:bg-slate-100 dark:hover:bg-slate-800' : ''"
      active-class="text-brand dark:text-brand-light"
      @click="$emit('navegar')"
    >
      {{ item.rotulo }}
    </RouterLink>
  </nav>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

// Propriedade: quando true, os links ficam empilhados (uso no celular)
defineProps({
  vertical: { type: Boolean, default: false }
})

// Evento avisando o cabeçalho de que um link foi clicado (para fechar o menu)
defineEmits(['navegar'])

// Links do site público — o painel interno tem a própria Sidebar
const itens = ref([
  { rotulo: 'Início', to: '/' },
  { rotulo: 'Sobre', to: '/#sobre' },
  { rotulo: 'Contato', to: '/#contato' },
  { rotulo: 'Acessar sistema', to: '/login' }
])
</script>
