import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api, { configurarAutenticacao } from '../services/api.js'
import { useUploadStore } from './uploadStore.js'

const CHAVE = 'cti-auth-v1'
export const useAuthStore = defineStore('auth', () => {
  let salva = null
  try { salva = JSON.parse(sessionStorage.getItem(CHAVE) || 'null') } catch { /* Sessão indisponível ou inválida. */ }
  const token = ref(typeof salva?.token === 'string' ? salva.token : null)
  const usuario = ref(null) // O perfil é sempre confirmado pela API após F5.
  const logado = computed(() => !!token.value && usuario.value?.status === 'ATIVO')
  const isAdmin = computed(() => logado.value && usuario.value?.perfil === 'ADMIN')
  let verificacao = null

  function logout() {
    token.value = null
    usuario.value = null
    try { sessionStorage.removeItem(CHAVE) } catch { /* Segue em memória. */ }
    useUploadStore().limpar()
  }
  function aceitarUsuario(dado) {
    if (!dado?.id || !dado.nome || !['USER', 'ADMIN'].includes(dado.perfil) || dado.status !== 'ATIVO') {
      throw new Error('A API não retornou um usuário ativo válido.')
    }
    usuario.value = { id: dado.id, nome: dado.nome, email: dado.email, perfil: dado.perfil, status: dado.status }
  }
  async function login(email, senha) {
    logout()
    const { data } = await api.post('/auth/login', { email: email.trim(), senha })
    try {
      if (typeof data.token !== 'string' || !data.token.trim()) throw new Error('A API não retornou um token válido.')
      aceitarUsuario(data.usuario)
      token.value = data.token
      try { sessionStorage.setItem(CHAVE, JSON.stringify({ token: token.value })) } catch { /* Login continua em memória. */ }
    } catch (erro) { logout(); throw erro }
  }
  async function restaurar() {
    if (!token.value || usuario.value) return
    if (!verificacao) verificacao = (async () => {
      const atual = token.value
      try {
        const { data } = await api.get('/auth/me')
        if (token.value === atual) aceitarUsuario(data)
      } catch (erro) { logout(); throw erro }
      finally { verificacao = null }
    })()
    return verificacao
  }
  configurarAutenticacao(() => token.value, logout)
  return { token, usuario, logado, isAdmin, login, logout, restaurar }
})
