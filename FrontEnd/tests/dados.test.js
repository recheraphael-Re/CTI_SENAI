import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import * as XLSX from 'xlsx'
import { lerPlanilha } from '../src/utils/planilha.js'
import { interpretarNumero } from '../src/utils/formato.js'
import { contar, evolucao, estatisticas } from '../src/utils/analise.js'

const arquivo = bytes => ({ arrayBuffer: async () => bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) })
const cabecalho = ['codigo_cliente', 'cliente', 'consultor', 'segmento', 'nivel', 'faturamento_anual', 'servico']
const linha = ['C1', 'Empresa', 'Ana', 'IND.', 'A', 12000, 'Internet Dedicada']
function planilha(colunas = cabecalho, dados = [linha]) {
  const pasta = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(pasta, XLSX.utils.aoa_to_sheet([colunas, ...dados]), 'dados_importacao')
  return arquivo(XLSX.write(pasta, { type: 'buffer', bookType: 'xlsx' }))
}

test('base oficial preserva todas as ocorrências do gabarito', async t => {
  const caminho = new URL('../../Data/CTI_Insights_Base_Validacao_300_Registros.xlsx', import.meta.url)
  if (!fs.existsSync(caminho)) return t.skip('Base oficial não está neste checkout')
  const bytes = fs.readFileSync(caminho)
  const resultado = await lerPlanilha(arquivo(bytes))
  assert.deepEqual(resultado.resumo, { aba: 'dados_importacao', linhas: 300, validas: 265, comErro: 35, ocorrencias: 37 })
  const pasta = XLSX.read(bytes, { type: 'buffer' })
  const esperado = XLSX.utils.sheet_to_json(pasta.Sheets.gabarito_erros).map(e => `${e.linha_excel}|${e.tipo_erro}|${e.campo}`).sort()
  assert.deepEqual(resultado.erros.map(e => `${e.linha}|${e.tipo}|${e.campo}`).sort(), esperado)
  assert.equal(resultado.periodoFaturamento, 'mensal')
})

test('números inválidos não viram zero e faixas preservam ponto médio', () => {
  for (const entrada of ['abc', '12abc', '1,2,3', '', 'de 100 a erro']) assert.equal(interpretarNumero(entrada), null)
  assert.equal(interpretarNumero('R$ 1.200,50'), 1200.5)
  assert.equal(interpretarNumero('-500'), -500)
  assert.equal(interpretarNumero('de 500 mil a 1 milhão'), 750000)
  assert.equal(interpretarNumero('0'), 0)
})

test('exige colunas e rejeita cabeçalhos ambíguos', async () => {
  await assert.rejects(lerPlanilha(planilha(['codigo_cliente', 'cliente'], [['C1', 'Empresa']])), /Colunas obrigatórias ausentes/)
  await assert.rejects(lerPlanilha(planilha([...cabecalho, 'faturamento_mensal'], [[...linha, 1000]])), /ambiguidade/)
})

test('preserva período anual, normaliza segmento e acusa valor inválido', async () => {
  const resultado = await lerPlanilha(planilha())
  assert.equal(resultado.periodoFaturamento, 'anual')
  assert.equal(resultado.clientes[0].faturamento, 12000)
  assert.equal(resultado.clientes[0].segmento, 'Indústria')
  const invalida = [...linha]; invalida[5] = 'abc'
  const erros = await lerPlanilha(planilha(cabecalho, [invalida]))
  assert.equal(erros.resumo.validas, 0)
  assert.equal(erros.erros[0].tipo, 'NUMERO_INVALIDO')
})

test('contagens, cronologia UTC e estatísticas usam os valores da seleção', () => {
  const lista = [
    { segmento: 'A', faturamento: 10, dataInicio: new Date('2026-02-01T00:00:00Z') },
    { segmento: 'A', faturamento: 20, dataInicio: new Date('2026-01-01T00:00:00Z') },
    { segmento: 'B', faturamento: 30, dataInicio: null }
  ]
  assert.deepEqual(contar(lista, 'segmento'), { rotulos: ['A', 'B'], valores: [2, 1] })
  assert.deepEqual(evolucao(lista), { rotulos: ['2026-01', '2026-02'], valores: [1, 1] })
  const resultado = estatisticas(lista)
  assert.equal(resultado.media, 20)
  assert.equal(resultado.mediana, 20)
  assert.ok(Math.abs(resultado.desvio - Math.sqrt(200 / 3)) < 1e-10)
  assert.equal(estatisticas([]), null)
})
