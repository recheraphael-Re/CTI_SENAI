<template>
  <!-- LOGIN — autenticação dos funcionários autorizados -->
  <section class="py-10 md:py-16 flex justify-center">
    <div class="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-lg p-8">
      <h2 class="text-2xl font-bold text-center text-slate-900 dark:text-slate-100">
        Bem-vindo de volta!
      </h2>
      <p class="text-center text-slate-600 dark:text-slate-400 mt-1 mb-6">
        Acesso demonstrativo — autenticação ainda não implementada. Não informe sua senha real.
      </p>

      <form class="flex flex-col gap-4" @submit.prevent="entrar">
        <div>
          <label for="email" class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1">
            E-mail corporativo
          </label>
          <input
            id="email"
            v-model="email" disabled
            type="email"
            placeholder="usuario@cti.com.br"
            class="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 text-slate-900 dark:text-slate-100 outline-none focus:border-brand"
          />
        </div>

        <div>
          <label for="senha" class="block text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1">
            Senha
          </label>
          <input
            id="senha"
            v-model="senha" disabled
            type="password"
            placeholder="••••••••"
            class="w-full rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 text-slate-900 dark:text-slate-100 outline-none focus:border-brand"
          />
        </div>

        <button
          type="submit"
          class="mt-2 bg-gradient-to-r from-brand to-brand-accent text-white px-6 py-3 rounded-lg hover:opacity-90 transition font-semibold"
        >
          Acessar demonstração
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const senha = ref('')

// Sem back-end ainda: apenas leva o usuário para a tela de upload
function entrar() {
  router.push('/app/upload')
}
</script>
