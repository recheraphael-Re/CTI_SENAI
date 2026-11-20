<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import ThemeToggle from './ThemeToggle.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const aberto = ref(false)
const links = computed(() => [
  { name: 'home', texto: 'Início' },
  ...(auth.logado ? [
    { name: 'upload', texto: 'Enviar planilha' },
    { name: 'relatorio', texto: 'Revisão' },
    { name: 'dashboard', texto: 'Dashboard' },
    ...(auth.isAdmin ? [{ name: 'admin', texto: 'Usuários' }] : [])
  ] : [
    { name: 'login', texto: 'Entrar' },
    { name: 'cadastro', texto: 'Criar conta' }
  ])
])

watch(() => route.fullPath, () => { aberto.value = false })

function sair() {
  aberto.value = false
  router.replace({ name: 'login' })
  auth.logout()
}
</script>

<template>
  <header class="border-b border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
    <div class="mx-auto flex max-w-6xl flex-wrap items-center gap-x-5 px-4 sm:px-6 lg:px-8">
      <RouterLink to="/" class="mr-auto py-4 text-xl font-bold text-brand dark:text-brand-light" @click="aberto = false">
        CTI Insights
      </RouterLink>
      <ThemeToggle class="md:order-2" />
      <button
        type="button"
        class="rounded-lg px-3 py-2 text-sm font-semibold text-brand hover:bg-brand/10 md:hidden dark:text-brand-light"
        aria-controls="menu-principal"
        :aria-expanded="aberto"
        @click="aberto = !aberto"
      >
        {{ aberto ? 'Fechar menu' : 'Menu' }}
      </button>
      <nav
        id="menu-principal"
        aria-label="Navegação principal"
        class="w-full flex-col gap-1 border-t border-slate-200 py-2 md:flex md:w-auto md:flex-row md:items-center md:border-0 md:py-0 dark:border-slate-800"
        :class="aberto ? 'flex' : 'hidden'"
      >
        <RouterLink
          v-for="link in links"
          :key="link.name"
          :to="{ name: link.name }"
          class="rounded-lg px-3 py-2 text-sm font-medium hover:bg-brand/10"
          exact-active-class="bg-brand/10 text-brand dark:text-brand-light"
          @click="aberto = false"
        >
          {{ link.texto }}
        </RouterLink>
        <button v-if="auth.logado" type="button" class="rounded-lg px-3 py-2 text-left text-sm font-semibold hover:bg-brand/10" @click="sair">
          Sair
        </button>
      </nav>
    </div>
  </header>
</template>
