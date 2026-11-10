// Usa exatamente o leitor do Vue para preparar o pedido de teste do backend.
import fs from 'node:fs'
import { lerPlanilha } from '../FrontEnd/src/utils/planilha.js'
const bytes = fs.readFileSync(new URL('../sample-data/dados.xlsx', import.meta.url))
const r = await lerPlanilha({ name: 'dados.xlsx', size: bytes.length,
  arrayBuffer: async () => bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) })
const linhas = r.clientes.filter(c => c.valida).map(c => ({ numeroLinha: c.linha,
  codigoCliente: c.id, nomeCliente: c.empresa, consultor: c.consultor, segmento: c.segmento,
  nivelCliente: c.nivel, faturamentoAnual: c.faturamento, servicosContratados: c.servico,
  dataContratacao: c.dataInicio.toISOString().slice(0, 10), cidade: c.cidade, uf: c.uf }))
process.stdout.write(JSON.stringify({ arquivoNome: 'dados.xlsx', descartadas: r.resumo.comErro, linhas }))
