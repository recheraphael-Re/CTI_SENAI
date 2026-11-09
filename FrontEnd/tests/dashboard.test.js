import test from 'node:test'
import assert from 'node:assert/strict'
import { createPinia, setActivePinia } from 'pinia'
import api from '../src/services/api.js'
import { useDashboardStore } from '../src/stores/dashboard.js'

test('dashboard usa último lote processado e permite consultar histórico', async () => {
  setActivePinia(createPinia())
  const store = useDashboardStore()
  const original = api.get
  try {
    api.get = async (url, config) => {
      if (url.endsWith('/lotes')) return { data: [{ id: 3, status: 'ERRO' }, { id: 2, status: 'PROCESSADO' }] }
      if (url.endsWith('/clientes')) return { data: [{ codigo: 'CTI1' }] }
      return { data: { lote: { id: config.params.loteId }, estatisticas: { resumo: {} } } }
    }
    await store.carregar()
    assert.equal(store.resultado.lote.id, 2)
    assert.equal(store.clientes[0].codigo, 'CTI1')
    await store.carregar(1)
    assert.equal(store.resultado.lote.id, 1)
    api.get = async () => { throw new Error('Servidor indisponível') }
    await store.carregar()
    assert.equal(store.resultado, null)
    assert.equal(store.clientes.length, 0)
    assert.match(store.erro, /indisponível/)
  } finally { api.get = original }
})

test('limpar sessão impede restauração de dados por uma resposta atrasada', async () => {
  setActivePinia(createPinia())
  const store = useDashboardStore()
  const original = api.get
  let concluir
  try {
    api.get = () => new Promise(resolve => { concluir = resolve })
    const carregamento = store.carregar()
    store.limpar()
    concluir({ data: [{ id: 1, status: 'PROCESSADO' }] })
    await carregamento
    assert.equal(store.resultado, null)
    assert.equal(store.lotes.length, 0)
    assert.equal(store.carregando, false)
  } finally { api.get = original }
})
