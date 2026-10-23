import test from 'node:test'
import assert from 'node:assert/strict'
import { createPinia, setActivePinia } from 'pinia'
import api, { mensagemErro } from '../src/services/api.js'
import { useAuthStore } from '../src/stores/auth.js'
import { useUploadStore } from '../src/stores/uploadStore.js'
import { criarProtecao } from '../src/router/protecao.js'

const usuario = { id: 1, nome: 'Ana', email: 'ana@example.com', perfil: 'ADMIN', status: 'ATIVO' }
function preparar(token) {
  const dados = new Map(token ? [['cti-auth-v1', JSON.stringify({ token })]] : [])
  globalThis.sessionStorage = { getItem: k => dados.get(k) ?? null, setItem: (k,v) => dados.set(k,v), removeItem: k => dados.delete(k) }
  setActivePinia(createPinia())
  return useAuthStore()
}
function responder(data) {
  api.defaults.adapter = async config => ({ data, status: 200, statusText: 'OK', headers: {}, config })
}
test('login valida resposta, guarda apenas token e logout limpa dados importados', async () => {
  const auth = preparar()
  responder({ token: 'token-teste', usuario })
  await auth.login(' ana@example.com ', 'senha-ficticia')
  assert.equal(auth.isAdmin, true)
  assert.deepEqual(JSON.parse(sessionStorage.getItem('cti-auth-v1')), { token: 'token-teste' })
  useUploadStore().clientes = [{ id: 'cliente' }]
  auth.logout()
  assert.equal(auth.logado, false)
  assert.equal(useUploadStore().clientes.length, 0)
  responder({ token: 'invalido', usuario: { ...usuario, status: 'PENDENTE' } })
  await assert.rejects(auth.login('ana@example.com', 'teste'))
  assert.equal(auth.token, null)
})
test('F5 confirma perfil na API, envia Bearer e não confia no perfil armazenado', async () => {
  const auth = preparar('restaurado')
  assert.equal(auth.isAdmin, false)
  api.defaults.adapter = async config => {
    assert.equal(config.url, '/auth/me')
    assert.equal(config.headers.Authorization, 'Bearer restaurado')
    return { data: { ...usuario, perfil: 'USER' }, status: 200, headers: {}, config }
  }
  await auth.restaurar()
  assert.equal(auth.logado, true)
  assert.equal(auth.isAdmin, false)
})
test('401 encerra sessão; falha de conexão não cria login', async () => {
  const auth = preparar('expirado')
  api.defaults.adapter = async config => { throw { config, response: { status: 401 } } }
  await assert.rejects(auth.restaurar())
  assert.equal(auth.token, null)
  api.defaults.adapter = async () => { throw { isAxiosError: true } }
  await assert.rejects(auth.login('ana@example.com', 'teste'))
  assert.equal(auth.logado, false)
  assert.match(mensagemErro({ isAxiosError: true }), /conectar/)
  assert.match(mensagemErro({ response: { data: { code: 'USUARIO_PENDENTE' } } }), /aprovação/)
})
test('rotas públicas, privadas, ADMIN e sessão inválida', async () => {
  const auth = preparar()
  const proteger = criarProtecao(auth)
  assert.equal(await proteger({ meta: {}, fullPath: '/' }), undefined)
  assert.deepEqual(await proteger({ meta: { requiresAuth: true }, fullPath: '/app/upload' }), { name: 'login', query: { redirect: '/app/upload' } })
  responder({ token: 'valido', usuario: { ...usuario, perfil: 'USER' } })
  await auth.login('ana@example.com', 'teste')
  assert.deepEqual(await proteger({ meta: { requiresAuth: true, admin: true } }), { name: 'graficos' })
  assert.equal(await proteger({ meta: { requiresAuth: true } }), undefined)
  auth.usuario = null
  api.defaults.adapter = async () => { throw new Error('Sem conexão') }
  assert.deepEqual(await proteger({ meta: { requiresAuth: true } }), { name: 'login', query: { motivo: 'sessao' } })
})
