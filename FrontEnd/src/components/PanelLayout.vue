<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import ThemeToggle from './ThemeToggle.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const larguraDesktop = window.matchMedia('(min-width: 48rem)')
const sidebarAberta = ref(larguraDesktop.matches)
const botaoMenu = ref(null)

const links = computed(() => [
  { name: 'home', texto: 'Início', icone: '🏠' },
  { name: 'upload', texto: 'Enviar planilha', icone: '📤' },
  { name: 'relatorio', texto: 'Revisão', icone: '📋' },
  { name: 'dashboard', texto: 'Dashboard', icone: '📈' },
  ...(auth.isAdmin ? [{ name: 'admin', texto: 'Usuários', icone: '👥' }] : [])
])

function ajustarMenu(evento) {
  sidebarAberta.value = evento.matches
}

function fecharNoCelular() {
  if (!larguraDesktop.matches) sidebarAberta.value = false
}

function fecharComFoco() {
  if (larguraDesktop.matches || !sidebarAberta.value) return
  fecharNoCelular()
  botaoMenu.value?.focus()
}

function sair() {
  router.replace({ name: 'login' })
  auth.logout()
}

onMounted(() => larguraDesktop.addEventListener('change', ajustarMenu))
onUnmounted(() => larguraDesktop.removeEventListener('change', ajustarMenu))
watch(() => route.fullPath, fecharNoCelular)
</script>

<template>
  <div
    class="min-h-screen motion-safe:transition-[padding] motion-safe:duration-300"
    :class="sidebarAberta ? 'md:pl-60' : 'md:pl-16'"
    @keydown.esc="fecharComFoco"
  >
    <header class="sticky top-0 z-40 flex h-16 items-center justify-between gap-4 border-b border-slate-200 bg-white px-4 dark:border-slate-800 dark:bg-slate-900">
      <div class="flex min-w-0 items-center gap-3">
        <button
          ref="botaoMenu"
          type="button"
          class="shrink-0 rounded-lg p-2 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-brand dark:hover:bg-slate-800"
          aria-controls="menu-painel"
          :aria-expanded="sidebarAberta"
          :aria-label="sidebarAberta ? 'Recolher menu' : 'Expandir menu'"
          @click="sidebarAberta = !sidebarAberta"
        >
          <svg aria-hidden="true" class="size-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <span class="truncate font-bold">{{ links.find(link => link.name === route.name)?.texto }}</span>
      </div>
      <div class="flex shrink-0 items-center gap-3">
        <ThemeToggle />
        <span class="hidden text-sm font-medium text-slate-600 sm:block dark:text-slate-300">{{ auth.usuario?.nome }}</span>
        <span aria-hidden="true" class="grid size-9 place-items-center rounded-full bg-linear-to-br from-brand to-brand-accent font-bold text-white">
          {{ auth.usuario?.nome.charAt(0).toUpperCase() }}
        </span>
      </div>
    </header>

    <div v-if="sidebarAberta" aria-hidden="true" class="fixed inset-0 z-30 bg-black/40 md:hidden" @click="fecharComFoco"></div>
    <aside
      id="menu-painel"
      class="fixed inset-y-0 left-0 top-16 z-35 flex flex-col overflow-hidden border-r border-slate-200 bg-white motion-safe:transition-[width] motion-safe:duration-300 md:top-0 md:z-50 dark:border-slate-800 dark:bg-slate-900"
      :class="sidebarAberta ? 'w-60' : 'invisible w-0 md:visible md:w-16'"
    >
      <div class="flex h-16 shrink-0 items-center gap-3 px-5 font-bold text-brand dark:text-brand-light">
        <span aria-hidden="true">📊</span>
        <span v-show="sidebarAberta" class="whitespace-nowrap bg-linear-to-r from-brand to-brand-accent bg-clip-text text-transparent dark:from-brand-light dark:to-brand-accent-light">CTI Insights</span>
      </div>
      <nav class="flex flex-1 flex-col gap-1 overflow-y-auto px-2 py-2" aria-label="Navegação do painel">
        <RouterLink
          v-for="link in links"
          :key="link.name"
          :to="{ name: link.name }"
          :title="link.texto"
          class="flex min-h-10 shrink-0 items-center gap-3 whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium hover:bg-slate-100 focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-brand dark:hover:bg-slate-800"
          exact-active-class="bg-brand/10 font-semibold text-brand dark:text-brand-light"
          @click="fecharNoCelular"
        >
          <span aria-hidden="true" class="w-6 shrink-0 text-center text-lg leading-none">{{ link.icone }}</span>
          <span :class="{ 'sr-only': !sidebarAberta }">{{ link.texto }}</span>
        </RouterLink>
      </nav>
      <div class="border-t border-slate-200 p-2 dark:border-slate-800">
        <button type="button" title="Sair" class="flex min-h-10 w-full items-center gap-3 rounded-lg px-3 text-left text-sm font-semibold hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-brand dark:hover:bg-slate-800" @click="sair">
          <span aria-hidden="true" class="w-6 shrink-0 text-center text-lg">↩</span>
          <span :class="{ 'sr-only': !sidebarAberta }">Sair</span>
        </button>
      </div>
    </aside>

    <main id="conteudo" class="mx-auto w-full min-w-0 max-w-[1440px] px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
      <slot />
    </main>
  </div>
</template>
