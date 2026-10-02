import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { createPinia, setActivePinia } from 'pinia'
import { useUploadStore } from '../src/stores/uploadStore.js'

test('store guarda seleção, processa base oficial e descarta dados', async () => {
  setActivePinia(createPinia())
  const store = useUploadStore()
  assert.equal(await store.processarPlanilha(), false)
  assert.match(store.erro, /Selecione/)
  assert.equal(store.selecionarArquivo({ name: 'arquivo.txt' }), false)
  assert.equal(store.arquivo, null)

  const caminho = new URL('../../Data/CTI_Insights_Base_Validacao_300_Registros.xlsx', import.meta.url)
  if (fs.existsSync(caminho)) {
    const bytes = fs.readFileSync(caminho)
    const arquivo = {
      name: 'clientes.xlsx',
      arrayBuffer: async () => bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength)
    }
    assert.equal(store.selecionarArquivo(arquivo), true)
    assert.equal(store.arquivo, arquivo)
    assert.equal(await store.processarPlanilha(), true)
    assert.equal(store.dadosOriginais.length, 300)
    assert.equal(store.dadosTratados.length, 300)
    assert.equal(store.totalClientes, 265)
    assert.equal(store.totalErros, 37)
    assert.equal(store.clientesNivelA, store.linhasValidas.filter(c => c.nivel === 'A').length)
    assert.equal(store.carregando, false)
  }

  store.limpar()
  assert.equal(store.arquivo, null)
  assert.equal(store.dadosOriginais.length, 0)
  assert.equal(store.dadosTratados.length, 0)
  assert.equal(store.totalClientes, 0)
  assert.equal(store.totalErros, 0)
  assert.equal(store.temDados, false)
})

test('falha de leitura encerra carregamento e informa erro', async () => {
  setActivePinia(createPinia())
  const store = useUploadStore()
  store.selecionarArquivo({ name: 'quebrada.csv', arrayBuffer: async () => { throw new Error('Falha ao ler arquivo') } })
  assert.equal(await store.processarPlanilha(), false)
  assert.equal(store.carregando, false)
  assert.equal(store.erro, 'Falha ao ler arquivo')
  assert.equal(store.temDados, false)
})
