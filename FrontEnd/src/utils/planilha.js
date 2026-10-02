// LEITURA E TRATAMENTO DA PLANILHA — converte o .xlsx/.csv em objetos JavaScript,
// padroniza o que dá para padronizar e aponta o que está errado.
//
// A base oficial (CTI_Insights_Base_Validacao_300_Registros.xlsx) tem erros
// plantados de propósito e uma aba "gabarito_erros" com o resultado esperado.
// As regras abaixo reproduzem esse gabarito.
import { normalizar, interpretarNumero } from './formato.js'

// A planilha oficial tem 5 abas e manda importar só esta
const ABA_DADOS = 'dados_importacao'
const ABA_LISTAS = 'listas_referencia'

// Domínios válidos. Quando a planilha traz a aba listas_referencia, ela manda;
// isto aqui é o que vale para planilhas que não tenham essa aba.
const LISTAS_PADRAO = {
  niveis: ['A', 'B', 'C'],
  status: ['ATIVO', 'PENDENTE', 'CANCELADO'],
  segmentos: [
    'Varejo', 'Indústria', 'Saúde', 'Tecnologia', 'Serviços',
    'Logística', 'Educação', 'Construção', 'Financeiro', 'Agronegócio'
  ],
  servicos: [
    'Internet Dedicada', 'Link Corporativo', 'Telefonia IP', 'Cloud Backup',
    'Firewall Gerenciado', 'SD-WAN', 'Backup Corporativo', 'Wi-Fi Corporativo'
  ]
}

// Abreviações que tirar acento e caixa não resolve sozinho
const APELIDOS_SEGMENTO = { ind: 'Indústria', tech: 'Tecnologia' }

// Nomes que cada coluna pode ter na planilha, já sem acento e sem espaço.
// Para aceitar uma variação nova, basta acrescentá-la na lista certa.
const COLUNAS = {
  id: ['codigo', 'codigocliente', 'codigodocliente', 'codcliente', 'id', 'idcliente'],
  empresa: ['empresa', 'cliente', 'nome', 'nomeempresa', 'nomedocliente', 'razaosocial', 'nomefantasia'],
  consultor: ['consultor', 'vendedor', 'responsavel', 'executivo'],
  segmento: ['segmento', 'setor', 'ramo', 'areadeatuacao', 'atividade'],
  nivel: ['nivel', 'classificacao', 'classe', 'categoria', 'porte'],
  faturamento: ['faturamento', 'faturamentomensal', 'faturamentoanual', 'faixadefaturamento', 'faixa', 'receita'],
  servico: ['servico', 'servicos', 'servicocontratado', 'produto', 'plano'],
  valorServico: ['valorservico', 'valordoservico', 'mensalidade', 'valor'],
  status: ['status', 'statuscontrato', 'situacao'],
  dataInicio: ['datainicio', 'datadeinicio', 'inicio', 'data']
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

// Troca a grafia da célula pela oficial: "SAUDE", "Saude" e "saúde" viram "Saúde".
// Não achando equivalente, devolve o texto como veio (quem acusa isso é a validação).
function padronizar(valor, oficiais, apelidos = {}) {
  const chave = normalizar(valor)
  if (!chave) return ''
  if (apelidos[chave]) return apelidos[chave]
  return oficiais.find((oficial) => normalizar(oficial) === chave) ?? String(valor).trim()
}

// Lê os domínios da aba listas_referencia, caindo no padrão quando ela não existe
function lerListas(XLSX, pasta) {
  const nome = pasta.SheetNames.find((n) => normalizar(n) === normalizar(ABA_LISTAS))
  if (!nome) return LISTAS_PADRAO

  const linhas = XLSX.utils.sheet_to_json(pasta.Sheets[nome], {
    header: 1,
    blankrows: true,
    defval: ''
  })

  // Cada coluna da aba é uma lista; a primeira linha é o título
  const coluna = (indice) =>
    linhas.slice(1).map((l) => String(l[indice] ?? '').trim()).filter(Boolean)

  return {
    niveis: coluna(0).length ? coluna(0) : LISTAS_PADRAO.niveis,
    status: coluna(1).length ? coluna(1) : LISTAS_PADRAO.status,
    segmentos: coluna(2).length ? coluna(2) : LISTAS_PADRAO.segmentos,
    servicos: coluna(3).length ? coluna(3) : LISTAS_PADRAO.servicos
  }
}

// Converte a data, aceitando o número serial do Excel e os formatos escritos.
// Devolve null quando a data não existe de verdade — 31/02/2026, por exemplo.
export function interpretarData(valor) {
  if (typeof valor === 'number' && valor > 0) {
    // Serial do Excel: 25569 é a distância em dias entre 30/12/1899 e 01/01/1970
    const data = new Date(Math.round((valor - 25569) * 86400000))
    return Number.isNaN(data.getTime()) ? null : data
  }

  const texto = String(valor ?? '').trim()
  if (!texto) return null

  const br = texto.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/)
  const iso = texto.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/)

  let ano, mes, dia
  if (br) [, dia, mes, ano] = br
  else if (iso) [, ano, mes, dia] = iso
  else return null

  const data = new Date(Date.UTC(Number(ano), Number(mes) - 1, Number(dia)))

  // Confere se a data sobreviveu à conversão: 31/02 viraria 03/03 e cai fora
  const existe =
    data.getUTCFullYear() === Number(ano) &&
    data.getUTCMonth() === Number(mes) - 1 &&
    data.getUTCDate() === Number(dia)

  return existe ? data : null
}

// As cinco regras do gabarito. `vistos` guarda os códigos já lidos, para que
// a DUPLICIDADE caia na segunda ocorrência, e não na primeira.
function validar(registro, bruto, listas, vistos) {
  const erros = []
  const acusar = (tipo, campo, descricao) => erros.push({ tipo, campo, descricao })

  const obrigatorios = [
    ['codigo_cliente', registro.id, 'Código do cliente vazio.'],
    ['cliente', registro.empresa, 'Nome do cliente vazio.'],
    ['consultor', registro.consultor, 'Consultor vazio.'],
    ['servico', registro.servico, 'Serviço vazio.']
  ]
  for (const [campo, valor, descricao] of obrigatorios) {
    if (!String(valor).trim()) acusar('CAMPO_OBRIGATORIO', campo, descricao)
  }

  if (registro.id) {
    if (vistos.has(registro.id)) {
      acusar('DUPLICIDADE', 'codigo_cliente', `Código duplicado: ${registro.id}.`)
    } else {
      vistos.set(registro.id, registro.linha)
    }
  }

  // O domínio é conferido pelo texto EXATO da planilha, não pelo padronizado:
  // "ativo" em minúsculo é erro de domínio, mesmo aparecendo como "ATIVO" na tela.
  const nivelBruto = String(bruto.nivel ?? '').trim()
  if (nivelBruto && !listas.niveis.includes(nivelBruto)) {
    acusar('DOMINIO_INVALIDO', 'nivel', `Nível fora da lista: ${nivelBruto}.`)
  }

  const statusBruto = String(bruto.status ?? '').trim()
  if (statusBruto && !listas.status.includes(statusBruto)) {
    acusar('DOMINIO_INVALIDO', 'status_contrato', `Status fora da lista: ${statusBruto}.`)
  }

  for (const [campo, valor] of [['faturamento', registro.faturamento], ['valor_servico', registro.valorServico]]) {
    const original = campo === 'faturamento' ? bruto.faturamento : bruto.valorServico
    if (valor === null && String(original ?? '').trim()) acusar('NUMERO_INVALIDO', campo, 'Informe um número ou uma faixa numérica válida.')
  }
  for (const campo of ['segmento', 'nivel', 'faturamento']) {
    if (!String(bruto[campo] ?? '').trim()) acusar('CAMPO_OBRIGATORIO', campo, `Campo ${campo} vazio.`)
  }

  if (registro.faturamento < 0) {
    acusar('VALOR_NEGATIVO', 'faturamento_mensal', 'Faturamento mensal negativo.')
  }
  if (registro.valorServico < 0) {
    acusar('VALOR_NEGATIVO', 'valor_servico', 'Valor do serviço negativo.')
  }

  if (String(bruto.dataInicio ?? '').trim() && registro.dataInicio === null) {
    acusar('DATA_INVALIDA', 'data_inicio', `Data inválida: ${bruto.dataInicio}.`)
  }

  return erros
}

// Lê o arquivo escolhido no navegador. Roda 100% no front: nada é enviado
// para servidor nenhum. Devolve as linhas, os erros e o resumo da importação.
export async function lerPlanilha(arquivo) {
  // import() dinâmico: o SheetJS (grande) só é baixado quando alguém envia
  // uma planilha de verdade, e não no primeiro carregamento do site.
  const XLSX = await import('xlsx')

  const buffer = await arquivo.arrayBuffer()
  const pasta = XLSX.read(buffer, { type: 'array' })

  if (pasta.SheetNames.length === 0) throw new Error('A planilha não tem nenhuma aba.')

  // A base oficial manda importar só a aba de dados; em outras planilhas,
  // vale a primeira aba mesmo.
  const nomeAba =
    pasta.SheetNames.find((n) => normalizar(n) === normalizar(ABA_DADOS)) ?? pasta.SheetNames[0]

  const listas = lerListas(XLSX, pasta)

  // header: 1 devolve linhas cruas (vetores), para localizar o cabeçalho na mão
  const linhas = XLSX.utils.sheet_to_json(pasta.Sheets[nomeAba], {
    header: 1,
    blankrows: true,
    defval: ''
  })

  const indice = acharCabecalho(linhas)
  if (indice === -1) {
    throw new Error(
      'Não encontrei as colunas esperadas (cliente, segmento, faturamento, nível...). ' +
      'Confira se a planilha tem a linha de cabeçalho.'
    )
  }

  // Posição de cada campo conhecido dentro da linha
  const mapa = {}
  linhas[indice].forEach((celula, coluna) => {
    const campo = campoDaColuna(celula)
    if (campo && mapa[campo] !== undefined) throw new Error(`Mais de uma coluna corresponde a ${campo}. Mantenha apenas uma para evitar ambiguidade.`)
    if (campo) mapa[campo] = coluna
  })

  const obrigatorias = { id: 'Código do cliente', empresa: 'Empresa', consultor: 'Consultor', segmento: 'Segmento', nivel: 'Nível', faturamento: 'Faturamento', servico: 'Serviço' }
  const ausentes = Object.entries(obrigatorias).filter(([campo]) => mapa[campo] === undefined).map(([, nome]) => nome)
  if (ausentes.length) throw new Error(`Colunas obrigatórias ausentes: ${ausentes.join(', ')}.`)
  const cabecalhoFaturamento = normalizar(linhas[indice][mapa.faturamento])
  const periodoFaturamento = cabecalhoFaturamento.includes('anual') ? 'anual' : cabecalhoFaturamento.includes('mensal') ? 'mensal' : 'não informado'

  const vistos = new Map()
  const clientes = []
  const dadosOriginais = []
  const erros = []

  linhas.slice(indice + 1).forEach((linha, posicao) => {
    const pegar = (campo) => (mapa[campo] === undefined ? '' : linha[mapa[campo]] ?? '')

    const bruto = {
      id: pegar('id'),
      empresa: pegar('empresa'),
      consultor: pegar('consultor'),
      segmento: pegar('segmento'),
      nivel: pegar('nivel'),
      faturamento: pegar('faturamento'),
      servico: pegar('servico'),
      valorServico: pegar('valorServico'),
      status: pegar('status'),
      dataInicio: pegar('dataInicio')
    }

    // Linha inteiramente vazia ou rodapé de total: não é registro
    const temConteudo = Object.values(bruto).some((v) => String(v).trim())
    if (!temConteudo || normalizar(bruto.empresa) === 'total') return
    dadosOriginais.push({ ...bruto })

    const registro = {
      // Linha do Excel: o +2 cobre o cabeçalho e a contagem começando em 1,
      // para bater com a coluna linha_excel do gabarito
      linha: indice + posicao + 2,
      id: String(bruto.id).trim(),
      empresa: String(bruto.empresa).trim(),
      consultor: String(bruto.consultor).trim(),
      segmento: padronizar(bruto.segmento, listas.segmentos, APELIDOS_SEGMENTO) || 'Não informado',
      segmentoOriginal: String(bruto.segmento).trim(),
      nivel: padronizar(bruto.nivel, listas.niveis) || '-',
      faturamento: interpretarNumero(bruto.faturamento),
      faturamentoOriginal: String(bruto.faturamento).trim(),
      servico: padronizar(bruto.servico, listas.servicos),
      valorServico: interpretarNumero(bruto.valorServico),
      status: padronizar(bruto.status, listas.status) || 'NÃO INFORMADO',
      statusOriginal: String(bruto.status).trim(),
      dataInicio: interpretarData(bruto.dataInicio)
    }

    registro.erros = validar(registro, bruto, listas, vistos)
    registro.valida = registro.erros.length === 0
    registro.ativo = registro.status === 'ATIVO'

    for (const erro of registro.erros) erros.push({ linha: registro.linha, ...erro })
    clientes.push(registro)
  })

  if (clientes.length === 0) {
    throw new Error('A planilha foi lida, mas nenhuma linha de cliente foi encontrada.')
  }

  return {
    dadosOriginais,
    periodoFaturamento,
    totalColunas: linhas[indice].filter(c => String(c).trim()).length,
    clientes,
    erros,
    resumo: {
      aba: nomeAba,
      linhas: clientes.length,
      validas: clientes.filter((c) => c.valida).length,
      comErro: clientes.filter((c) => !c.valida).length,
      ocorrencias: erros.length
    }
  }
}
