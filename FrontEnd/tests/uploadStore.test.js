import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { createPinia, setActivePinia } from 'pinia'
import { useUploadStore } from '../src/stores/uploadStore.js'
import api from '../src/services/api.js'
import * as XLSX from 'xlsx'

test('planilha da aula envia DTO anual, guarda retorno e mostra erro do servidor', async () => {
  setActivePinia(createPinia())
  const store = useUploadStore()
  const pasta = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(pasta, XLSX.utils.aoa_to_sheet([
    ['codigo_cliente', 'nome_cliente', 'consultor', 'segmento', 'nivel_cliente', 'faturamento_anual', 'servicos_contratados', 'data_contratacao', 'cidade', 'uf'],
    ['CTI001', 'Empresa', 'Ana Souza', 'IND.', 'A', 100, 'Internet Dedicada', '2025-01-15', 'Campinas', 'sp']
  ]), 'upload_clientes')
  const bytes = XLSX.write(pasta, { type: 'array', bookType: 'xlsx' })
  assert.equal(await store.importarPlanilha({ name: 'aula.xlsx', arrayBuffer: async () => bytes }), true)
  const original = api.post
  try {
    api.post = async (url, envio) => {
      assert.equal(url, '/api/uploads/validadas')
      assert.equal(envio.arquivoNome, 'aula.xlsx')
      assert.equal(envio.descartadas, 0)
      const dados = envio.linhas
      assert.equal(dados[0].numeroLinha, 2)
      assert.equal(dados[0].nomeCliente, 'Empresa')
      assert.equal(dados[0].faturamentoAnual, 100)
      assert.equal(dados[0].dataContratacao, '2025-01-15')
      assert.equal(dados[0].uf, 'sp')
      return { data: { linhas: dados, totalValidadas: dados.length, descartadas: 0 } }
    }
    assert.equal(await store.validarNoBackend(), true)
    assert.equal(store.clientesPreparados.length, 1)
    api.post = async () => { throw { response: { data: { message: 'Código repetido' } } } }
    assert.equal(await store.validarNoBackend(), false)
    assert.equal(store.erro, 'Código repetido')
    assert.equal(store.clientesPreparados.length, 0)
    store.periodoFaturamento = 'mensal'
    assert.equal(await store.validarNoBackend(), false)
    assert.match(store.erro, /faturamento_anual/)
  } finally { api.post = original }
})

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
