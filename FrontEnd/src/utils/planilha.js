// LEITURA DA PLANILHA — converte o .xlsx/.csv do cliente em objetos JavaScript.
// A planilha do Provedor CTI é preenchida à mão, então nada aqui pode depender
// da ordem das colunas nem da grafia exata do cabeçalho.
import { normalizar, interpretarNumero, interpretarStatus } from './formato.js'

// Nomes que cada coluna pode ter na planilha, já sem acento e sem espaço.
// Para aceitar uma variação nova, basta acrescentá-la na lista certa.
const COLUNAS = {
  id: ['codigo', 'codigocliente', 'codigodocliente', 'codcliente', 'id', 'idcliente'],
  empresa: ['empresa', 'cliente', 'nome', 'nomeempresa', 'nomedocliente', 'razaosocial', 'nomefantasia'],
  consultor: ['consultor', 'vendedor', 'responsavel', 'executivo'],
  segmento: ['segmento', 'setor', 'ramo', 'areadeatuacao', 'atividade'],
  faturamento: ['faturamento', 'faturamentoanual', 'faixadefaturamento', 'faixafaturamento', 'faixa', 'receita', 'receitaanual'],
  nivel: ['nivel', 'classificacao', 'classe', 'categoria', 'porte'],
  servicos: ['servicos', 'servico', 'servicoscontratados', 'produtos', 'planos'],
  status: ['status', 'situacao', 'ativo']
}

// Descobre a qual campo uma célula de cabeçalho corresponde (ou null).
function campoDaColuna(cabecalho) {
  const chave = normalizar(cabecalho)
  if (!chave) return null

  for (const [campo, apelidos] of Object.entries(COLUNAS)) {
    // startsWith aceita "faixadefaturamentoanual" a partir de "faixadefaturamento"
    if (apelidos.some((apelido) => chave === apelido || chave.startsWith(apelido))) {
      return campo
    }
  }
  return null
}

// Acha a linha do cabeçalho: planilhas feitas à mão costumam ter título,
// logo ou linhas em branco antes da tabela de verdade.
function acharCabecalho(linhas) {
  for (let i = 0; i < Math.min(linhas.length, 20); i++) {
    const campos = new Set(linhas[i].map(campoDaColuna).filter(Boolean))
    if (campos.size >= 2) return i
  }
  return -1
}

// Lê o arquivo escolhido no navegador e devolve a lista de clientes.
// Roda 100% no front: nada é enviado para servidor nenhum.
export async function lerPlanilha(arquivo) {
  // import() dinâmico: o SheetJS (grande) só é baixado quando alguém envia
  // uma planilha de verdade, e não no primeiro carregamento do site.
  const XLSX = await import('xlsx')

  const buffer = await arquivo.arrayBuffer()
  const pasta = XLSX.read(buffer, { type: 'array' })

  const nomeAba = pasta.SheetNames[0]
  if (!nomeAba) throw new Error('A planilha não tem nenhuma aba.')

  // header: 1 devolve linhas cruas (vetores), para localizar o cabeçalho na mão
  const linhas = XLSX.utils.sheet_to_json(pasta.Sheets[nomeAba], {
    header: 1,
    blankrows: false,
    defval: ''
  })

  const indice = acharCabecalho(linhas)
  if (indice === -1) {
    throw new Error(
      'Não encontrei as colunas esperadas (empresa, segmento, faturamento, nível...). ' +
      'Confira se a primeira aba da planilha tem a linha de cabeçalho.'
    )
  }

  // Posição de cada campo conhecido dentro da linha
  const mapa = {}
  linhas[indice].forEach((celula, coluna) => {
    const campo = campoDaColuna(celula)
    if (campo && mapa[campo] === undefined) mapa[campo] = coluna
  })

  const pegar = (linha, campo) => (mapa[campo] === undefined ? '' : linha[mapa[campo]] ?? '')

  const clientes = linhas
    .slice(indice + 1)
    .map((linha, posicao) => {
      const faturamentoOriginal = String(pegar(linha, 'faturamento')).trim()

      return {
        id: String(pegar(linha, 'id')).trim() || posicao + 1,
        empresa: String(pegar(linha, 'empresa')).trim(),
        consultor: String(pegar(linha, 'consultor')).trim(),
        segmento: String(pegar(linha, 'segmento')).trim() || 'Não informado',
        faturamento: interpretarNumero(pegar(linha, 'faturamento')),
        faturamentoOriginal,
        nivel: String(pegar(linha, 'nivel')).trim().toUpperCase() || '-',
        // Os serviços vêm numa célula só, separados por vírgula, ponto e vírgula ou barra
        servicos: String(pegar(linha, 'servicos'))
          .split(/[,;/|]/)
          .map((s) => s.trim())
          .filter(Boolean),
        ativo: interpretarStatus(pegar(linha, 'status'))
      }
    })
    // Descarta linhas vazias e totais no rodapé da planilha
    .filter((c) => c.empresa && normalizar(c.empresa) !== 'total')

  if (clientes.length === 0) {
    throw new Error('A planilha foi lida, mas nenhuma linha de cliente foi encontrada.')
  }

  return clientes
}
