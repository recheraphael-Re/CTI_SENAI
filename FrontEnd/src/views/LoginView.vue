<template>
  <!-- LOGIN — autenticação dos funcionários autorizados -->
  <section class="py-10 md:py-16 flex justify-center">
    <div class="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-lg p-8">
      <h2 class="text-2xl font-bold text-center text-slate-900 dark:text-slate-100">
        Bem-vindo de volta!
      </h2>
      <p class="text-center text-slate-600 dark:text-slate-400 mt-1 mb-6">
        Acesse a carteira de clientes e as análises da sua empresa.
      </p>

      <p v-if="route.query.motivo === 'sessao'" role="status" class="mb-4 text-sm text-slate-600 dark:text-slate-400">Sua sessão precisa ser validada. Entre novamente.</p>
      <p v-if="erro" role="alert" class="mb-4 text-sm text-red-600 dark:text-red-400">{{ erro }}</p>
      <button
        v-if="loginLocalDisponivel"
        type="button" :disabled="carregando" @click="entrarLocal"
        class="w-full bg-gradient-to-r from-brand to-brand-accent text-white px-6 py-3 rounded-lg hover:opacity-90 transition font-semibold"
      >
        {{ carregando ? 'Entrando...' : 'Entrar sem senha' }}
      </button>
      <form v-else class="flex flex-col gap-4" @submit.prevent="entrarComSenha">
        <div>
          <label for="email" class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1">
            E-mail corporativo
          </label>
          <input
            id="email"
            v-model="email" required autocomplete="username" maxlength="254"
            type="email"
            placeholder="seunome@empresa.com.br"
            class="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 text-slate-900 dark:text-slate-100 outline-none focus:border-brand"
          />
        </div>

        <div>
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
          {{ carregando ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
      <p class="mt-6 text-center text-sm text-slate-600 dark:text-slate-400">
        Ainda não tem uma conta?
        <RouterLink to="/cadastro" class="font-semibold text-brand hover:underline dark:text-brand-light">Cadastre-se</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import api, { mensagemErro } from '../services/api.js'
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const email = ref('')
const senha = ref('')
const erro = ref('')
const carregando = ref(false)
const loginLocalDisponivel = ref(false)

if (import.meta.env.DEV && ['localhost', '127.0.0.1'].includes(window.location.hostname)) {
  onMounted(async () => {
    try {
      const resposta = await api.get('/auth/login-local')
      loginLocalDisponivel.value = resposta.status === 204
    } catch { /* O formulário com senha continua disponível. */ }
  })
}

function entrarComSenha() {
  return entrar(() => auth.login(email.value, senha.value))
}

function entrarLocal() {
  return entrar(() => auth.loginLocal())
}

async function entrar(autenticar) {
  if (carregando.value) return
  carregando.value = true
  erro.value = ''
  try {
    await autenticar()
    const destino = route.query.redirect
    const rotaProtegida = typeof destino === 'string' && destino.startsWith('/') &&
      !destino.startsWith('//') && router.resolve(destino).meta.requiresAuth
    await router.replace(rotaProtegida ? destino : { name: 'upload' })
  } catch (e) { erro.value = mensagemErro(e) }
  finally { carregando.value = false; senha.value = '' }
}
</script>
