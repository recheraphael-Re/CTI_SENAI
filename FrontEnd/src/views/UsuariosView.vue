<script setup>
import { ref, onMounted } from 'vue'
import api, { mensagemErro } from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'
const auth = useAuthStore()
const perfis = ref({})
const usuarios = ref([])
const carregando = ref(false)
const alterando = ref(null)
const erro = ref('')
const mensagem = ref('')
async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    const { data } = await api.get('/admin/usuarios')
    if (!Array.isArray(data)) throw new Error('A API retornou uma lista de usuários inválida.')
    usuarios.value = data
    perfis.value = Object.fromEntries(data.map(u => [u.id, u.perfil]))
  } catch (e) { erro.value = mensagemErro(e) }
  finally { carregando.value = false }
}
async function alterar(usuario, status) {
  if (alterando.value !== null) return
  alterando.value = usuario.id
  erro.value = ''
  mensagem.value = ''
  try {
    await api.patch(`/admin/usuarios/${encodeURIComponent(usuario.id)}/status`, { status, perfil: perfis.value[usuario.id] })
    mensagem.value = `Perfil e status de ${usuario.nome} atualizados.`
    await carregar()
  } catch (e) { erro.value = mensagemErro(e) }
  finally { alterando.value = null }
}
onMounted(carregar)
</script>
<template>
  <section class="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-100">
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-bold">Solicitações e usuários</h1>
      <button class="rounded-lg bg-brand px-4 py-2 text-white disabled:opacity-50" :disabled="carregando || alterando !== null" @click="carregar">Atualizar lista</button>
    </div>
    <p v-if="erro" role="alert" class="mb-4 text-red-600 dark:text-red-400">{{ erro }}</p>
    <p class="mb-4 text-sm text-slate-600 dark:text-slate-400">Escolha Usuário ou Administrador antes de aprovar o cadastro. Administradores podem gerenciar contas e aprovações.</p>
    <p v-if="mensagem" role="status" class="mb-4 text-emerald-700 dark:text-emerald-400">{{ mensagem }}</p>
    <p v-if="carregando" role="status">Carregando usuários...</p>
    <p v-else-if="!usuarios.length && !erro">Nenhum usuário cadastrado.</p>
    <div v-else-if="usuarios.length" class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <caption class="sr-only">Usuários e ações de aprovação e bloqueio</caption>
        <thead><tr><th scope="col" class="p-3">Nome</th><th scope="col" class="p-3">E-mail</th><th scope="col" class="p-3">Perfil</th><th scope="col" class="p-3">Status</th><th scope="col" class="p-3">Ações</th></tr></thead>
        <tbody><tr v-for="u in usuarios" :key="u.id" class="border-t border-slate-200 dark:border-slate-700">
          <td class="p-3">{{ u.nome }}</td><td class="p-3">{{ u.email }}</td>
          <td class="p-3">
            <select v-model="perfis[u.id]" :aria-label="`Perfil de ${u.nome}`" :disabled="carregando || alterando !== null || u.id === auth.usuario?.id" class="rounded border border-slate-300 bg-white p-2 dark:border-slate-700 dark:bg-slate-800 disabled:opacity-50">
              <option value="USER">Usuário</option>
              <option value="ADMIN">Administrador</option>
            </select>
          </td><td class="p-3">{{ u.status }}</td>
          <td class="p-3"><div class="flex gap-2">
            <button class="rounded bg-emerald-700 px-3 py-2 text-white disabled:opacity-50" :disabled="carregando || alterando !== null || (u.status === 'ATIVO' && perfis[u.id] === u.perfil)" :aria-label="`Salvar e ativar ${u.nome}`" @click="alterar(u, 'ATIVO')">{{ u.status === 'ATIVO' ? 'Salvar perfil' : 'Aprovar e ativar' }}</button>
            <button class="rounded bg-red-700 px-3 py-2 text-white disabled:opacity-50" :disabled="carregando || alterando !== null || u.status === 'BLOQUEADO' || u.id === auth.usuario?.id" :aria-label="`Bloquear ${u.nome}`" @click="alterar(u, 'BLOQUEADO')">Bloquear</button>
          </div></td>
        </tr></tbody>
      </table>
    </div>
  </section>
</template>
