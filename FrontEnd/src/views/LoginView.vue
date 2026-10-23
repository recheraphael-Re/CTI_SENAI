<template>
  <!-- LOGIN — autenticação dos funcionários autorizados -->
  <section class="py-10 md:py-16 flex justify-center">
    <div class="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-lg p-8">
      <h2 class="text-2xl font-bold text-center text-slate-900 dark:text-slate-100">
        Bem-vindo de volta!
      </h2>
      <p class="text-center text-slate-600 dark:text-slate-400 mt-1 mb-6">
        {{ modoDemonstracao ? 'Demonstração para as aulas. Entre no painel sem e-mail ou senha.' : 'Entre com sua conta aprovada pelo administrador.' }}
      </p>

      <p v-if="!modoDemonstracao && route.query.motivo === 'sessao'" role="status" class="mb-4 text-sm text-slate-600 dark:text-slate-400">Sua sessão precisa ser validada. Entre novamente.</p>
      <p v-if="erro" role="alert" class="mb-4 text-sm text-red-600 dark:text-red-400">{{ erro }}</p>
      <form class="flex flex-col gap-4" @submit.prevent="entrar">
        <div v-if="!modoDemonstracao">
          <label for="email" class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1">
            E-mail corporativo
          </label>
          <input
            id="email"
            v-model="email" required autocomplete="username" maxlength="254"
            type="email"
            placeholder="usuario@cti.com.br"
            class="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 text-slate-900 dark:text-slate-100 outline-none focus:border-brand"
          />
        </div>

        <div v-if="!modoDemonstracao">
          <label for="senha" class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1">
            Senha
          </label>
          <input
            id="senha"
            v-model="senha" required autocomplete="current-password"
            type="password"
            placeholder="••••••••"
            class="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 text-slate-900 dark:text-slate-100 outline-none focus:border-brand"
          />
        </div>

        <button
          type="submit" :disabled="carregando"
          class="mt-2 bg-gradient-to-r from-brand to-brand-accent text-white px-6 py-3 rounded-lg hover:opacity-90 transition font-semibold"
        >
          {{ modoDemonstracao ? 'Acessar demonstração' : (carregando ? 'Entrando...' : 'Entrar') }}
        </button>
      </form>
      <p v-if="!modoDemonstracao" class="mt-6 text-center text-sm text-slate-600 dark:text-slate-400">
        Ainda não tem uma conta?
        <RouterLink to="/cadastro" class="font-semibold text-brand hover:underline dark:text-brand-light">Cadastre-se</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup>
import { modoDemonstracao } from '../utils/modoAcesso.js'
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { mensagemErro } from '../services/api.js'
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const email = ref('')
const senha = ref('')
const erro = ref('')
const carregando = ref(false)
async function entrar() {
  if (modoDemonstracao) { await router.push('/app/upload'); return }
  if (carregando.value) return
  carregando.value = true
  erro.value = ''
  try {
    await auth.login(email.value, senha.value)
    const destino = route.query.redirect
    await router.replace(typeof destino === 'string' && destino.startsWith('/app/') ? destino : '/app/graficos')
  } catch (e) { erro.value = mensagemErro(e) }
  finally { carregando.value = false; senha.value = '' }
}
</script>
