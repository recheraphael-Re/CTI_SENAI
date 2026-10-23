<template>
  <section class="flex justify-center py-10 md:py-16">
    <div class="w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-lg dark:border-slate-800 dark:bg-slate-900 sm:p-8">
      <h2 class="text-center text-2xl font-bold text-slate-900 dark:text-slate-100">Solicitar acesso</h2>
      <p class="mt-2 mb-6 text-center text-sm text-slate-600 dark:text-slate-400">
        {{ modoDemonstracao ? 'Cadastro temporariamente indisponível.' : 'Solicite sua conta e aguarde a aprovação do administrador.' }}
      </p>

      <div v-if="concluido" role="status" class="mb-6 rounded-lg bg-emerald-50 p-4 text-sm text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200">
        Solicitação enviada. Aguarde a aprovação do administrador.
      </div>

      <p v-if="modoDemonstracao" role="status" class="mb-4 text-sm text-slate-600 dark:text-slate-400">O cadastro ficará disponível nas próximas aulas, com o backend. Por enquanto, use <RouterLink to="/app/upload" class="font-semibold underline">o painel demonstrativo</RouterLink>.</p>
      <form v-if="!modoDemonstracao" class="flex flex-col gap-4" @submit.prevent="cadastrar" @input="concluido = false">
        <div>
          <label for="cadastro-nome" class="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">Nome completo</label>
          <input id="cadastro-nome" v-model="nome" required maxlength="120" autocomplete="name" placeholder="Seu nome completo" class="campo" />
        </div>
        <div>
          <label for="cadastro-email" class="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">E-mail corporativo</label>
          <input id="cadastro-email" v-model="email" required type="email" maxlength="254" autocomplete="email" placeholder="usuario@cti.com.br" class="campo" />
        </div>
        <div>
          <label for="cadastro-senha" class="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">Senha</label>
          <input id="cadastro-senha" v-model="senha" required :type="mostrarSenha ? 'text' : 'password'" minlength="8" autocomplete="new-password" aria-describedby="senha-ajuda" class="campo" />
          <p id="senha-ajuda" class="mt-1 text-xs text-slate-500 dark:text-slate-400">Use pelo menos 8 caracteres para sua senha.</p>
        </div>
        <div>
          <label for="cadastro-confirmacao" class="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">Confirme a senha</label>
          <input id="cadastro-confirmacao" v-model="confirmacao" required :type="mostrarSenha ? 'text' : 'password'" autocomplete="new-password" class="campo" />
        </div>
        <label class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
          <input v-model="mostrarSenha" type="checkbox" class="accent-brand" /> Mostrar senhas
        </label>
        <p v-if="erro" role="alert" class="text-sm text-red-600 dark:text-red-400">{{ erro }}</p>
        <button type="submit" :disabled="carregando" class="mt-2 rounded-lg bg-gradient-to-r from-brand to-brand-accent px-6 py-3 font-semibold text-white transition hover:opacity-90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand">
          {{ carregando ? 'Enviando...' : 'Enviar solicitação' }}
        </button>
      </form>
      <p class="mt-6 text-center text-sm text-slate-600 dark:text-slate-400">
        Já tem uma conta?
        <RouterLink to="/login" class="font-semibold text-brand hover:underline dark:text-brand-light">Ir para login</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup>
import { modoDemonstracao } from '../utils/modoAcesso.js'
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import api, { mensagemErro } from '../services/api.js'

const nome = ref('')
const email = ref('')
const senha = ref('')
const confirmacao = ref('')
const mostrarSenha = ref(false)
const erro = ref('')
const concluido = ref(false)
const carregando = ref(false)

async function cadastrar() {
  if (carregando.value) return
  erro.value = ''
  concluido.value = false
  if (!nome.value.trim()) {
    erro.value = 'Informe seu nome.'
    return
  }
  if (senha.value !== confirmacao.value) {
    erro.value = 'As senhas precisam ser iguais.'
    return
  }

  carregando.value = true
  try {
    await api.post('/auth/register', { nome: nome.value.trim(), email: email.value.trim(), senha: senha.value })
    concluido.value = true
  } catch (e) { erro.value = mensagemErro(e) }
  finally {
    carregando.value = false
    senha.value = ''
    confirmacao.value = ''
    mostrarSenha.value = false
  }
}
</script>

<style scoped>
@reference "../style.css";

.campo {
  @apply w-full rounded-lg border border-slate-300 bg-white px-4 py-2 text-slate-900 outline-none focus:border-brand focus:ring-2 focus:ring-violet-200 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100 dark:focus:ring-violet-900;
}
</style>
