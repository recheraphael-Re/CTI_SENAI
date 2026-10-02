import { ref, shallowRef, computed } from 'vue'
import { defineStore } from 'pinia'
import { lerPlanilha } from '../utils/planilha.js'
import { formatarMoeda } from '../utils/formato.js'

const CHAVE = 'cti-insights-clientes-v2'

// Enquanto ninguém enviou planilha, as telas mostram esta amostra,
// para o layout nunca aparecer vazio na apresentação.
const CLIENTES_EXEMPLO = [
  { linha: 2, id: 'CLI0001', empresa: 'TechCorp S.A.', consultor: 'Ana Souza', segmento: 'Tecnologia', segmentoOriginal: 'Tecnologia', nivel: 'A', faturamento: 120000, faturamentoOriginal: '', servico: 'Link Corporativo', valorServico: 2500, status: 'ATIVO', statusOriginal: 'ATIVO', dataInicio: null, erros: [], valida: true, ativo: true },
  { linha: 3, id: 'CLI0002', empresa: 'Indústrias ABC', consultor: 'Bruno Lima', segmento: 'Indústria', segmentoOriginal: 'Indústria', nivel: 'B', faturamento: 85000, faturamentoOriginal: '', servico: 'SD-WAN', valorServico: 1800, status: 'ATIVO', statusOriginal: 'ATIVO', dataInicio: null, erros: [], valida: true, ativo: true },
  { linha: 4, id: 'CLI0003', empresa: 'Comercial Silva', consultor: 'Ana Souza', segmento: 'Varejo', segmentoOriginal: 'Varejo', nivel: 'C', faturamento: 20000, faturamentoOriginal: '', servico: 'Internet Dedicada', valorServico: 600, status: 'CANCELADO', statusOriginal: 'CANCELADO', dataInicio: null, erros: [], valida: true, ativo: false },
  { linha: 5, id: 'CLI0004', empresa: 'Logística Express', consultor: 'Carla Mendes', segmento: 'Logística', segmentoOriginal: 'Logística', nivel: 'A', faturamento: 350000, faturamentoOriginal: '', servico: 'Cloud Backup', valorServico: 4200, status: 'ATIVO', statusOriginal: 'ATIVO', dataInicio: null, erros: [], valida: true, ativo: true },
  { linha: 6, id: 'CLI0005', empresa: 'Farma Horizonte', consultor: 'Bruno Lima', segmento: 'Saúde', segmentoOriginal: 'Saúde', nivel: 'B', faturamento: 42000, faturamentoOriginal: '', servico: 'Firewall Gerenciado', valorServico: 900, status: 'PENDENTE', statusOriginal: 'PENDENTE', dataInicio: null, erros: [], valida: true, ativo: false }
]

const RESUMO_VAZIO = { aba: '', linhas: 0, validas: 0, comErro: 0, ocorrencias: 0 }

// Recupera o último envio, para um F5 não apagar a planilha da sessão
function lerDaSessao() {
  try {
    const salvo = JSON.parse(sessionStorage.getItem(CHAVE) ?? 'null')
    if (!salvo?.clientes?.length) return null

    // O JSON não guarda Date: as datas voltam como texto e são reconvertidas
    for (const cliente of salvo.clientes) {
      cliente.dataInicio = cliente.dataInicio ? new Date(cliente.dataInicio) : null
    }
    return salvo
  } catch {
    return null
  }
}

export const useUploadStore = defineStore('upload', () => {
  const sessao = lerDaSessao()
  // File permanece em memória: não pode ser restaurado pelo JSON da sessão.
  const arquivo = shallowRef(null)
  const dadosOriginais = ref(sessao?.dadosOriginais ?? [])
  const clientes = ref(sessao?.clientes ?? [])
  const erros = ref(sessao?.erros ?? [])
  const resumo = ref(sessao?.resumo ?? { ...RESUMO_VAZIO })
  const nomeArquivo = ref(sessao?.nomeArquivo ?? '')
  const dataUpload = ref(sessao?.dataUpload ?? null)
  const totalColunas = ref(sessao?.totalColunas ?? 0)
  const periodoFaturamento = ref(sessao?.periodoFaturamento ?? 'não informado')
  const rotuloFaturamento = computed(() => periodoFaturamento.value === 'não informado' ? 'Faturamento (período não informado)' : `Faturamento ${periodoFaturamento.value}`)
  const carregando = ref(false)
  let operacao = 0
  const erro = ref('')


  // Há planilha carregada? Senão, as telas caem nos dados de exemplo.
  const temDados = computed(() => clientes.value.length > 0)
  const usandoExemplo = computed(() => !temDados.value)
  const clientesExibidos = computed(() => (temDados.value ? clientes.value : CLIENTES_EXEMPLO))

  // Os indicadores usam SÓ as linhas válidas: somar faturamento negativo ou
  // contar linha duplicada distorceria a análise que o cliente pediu.
  const clientesValidos = computed(() => clientesExibidos.value.filter((c) => c.valida))

  const faturamentoTotal = computed(() =>
    clientesValidos.value.reduce((soma, c) => soma + c.faturamento, 0)
  )

  const indicadores = computed(() => {
    const lista = clientesValidos.value
    const media = lista.length ? faturamentoTotal.value / lista.length : 0
    const ativos = lista.filter((c) => c.ativo).length

    return [
      { rotulo: 'Clientes válidos', valor: lista.length.toLocaleString('pt-BR') },
      { rotulo: `${rotuloFaturamento.value} médio`, valor: formatarMoeda(media) },
      { rotulo: 'Registros ativos', valor: ativos.toLocaleString('pt-BR') }
    ]
  })

  // Soma o faturamento por segmento e devolve pronto para o gráfico de barras
  const faturamentoPorSegmento = computed(() => {
    const soma = new Map()
    for (const cliente of clientesValidos.value) {
      soma.set(cliente.segmento, (soma.get(cliente.segmento) ?? 0) + cliente.faturamento)
    }

    const ordenado = [...soma.entries()].sort((a, b) => b[1] - a[1]).slice(0, 10)
    return {
      rotulos: ordenado.map(([segmento]) => segmento),
      valores: ordenado.map(([, valor]) => valor)
    }
  })

  // Agrupa as ocorrências por tipo, para o resumo da importação
  const errosPorTipo = computed(() => {
    const contagem = new Map()
    for (const e of erros.value) contagem.set(e.tipo, (contagem.get(e.tipo) ?? 0) + 1)
    return [...contagem.entries()]
      .sort((a, b) => b[1] - a[1])
      .map(([tipo, quantidade]) => ({ tipo, quantidade }))
  })

  function selecionarArquivo(novo) {
    if (carregando.value) return false
    erro.value = ''
    arquivo.value = null
    if (!novo) return false
    if (!/\.(xlsx|xls|csv)$/i.test(novo.name)) {
      erro.value = 'Selecione um arquivo XLSX, XLS ou CSV.'
      return false
    }
    arquivo.value = novo
    return true
  }

  function validarArquivo() {
    if (arquivo.value && /\.(xlsx|xls|csv)$/i.test(arquivo.value.name)) return true
    erro.value = 'Selecione um arquivo XLSX, XLS ou CSV.'
    return false
  }

  async function processarPlanilha() {
    return importarPlanilha(arquivo.value)
  }

  // O arquivo é capturado para que o nome corresponda aos dados processados.
  async function importarPlanilha(arquivoSelecionado) {
    if (carregando.value) return false
    erro.value = ''
    selecionarArquivo(arquivoSelecionado)
    if (!validarArquivo()) return false
    const atual = ++operacao
    carregando.value = true
    erro.value = ''

    try {
      const lido = await lerPlanilha(arquivoSelecionado)
      if (atual !== operacao) return false
      dataUpload.value = new Date().toISOString()
      periodoFaturamento.value = lido.periodoFaturamento
      totalColunas.value = lido.totalColunas
      clientes.value = lido.clientes
      dadosOriginais.value = lido.dadosOriginais
      erros.value = lido.erros
      resumo.value = lido.resumo
      nomeArquivo.value = arquivoSelecionado.name

      try {
        sessionStorage.setItem(CHAVE, JSON.stringify({ ...lido, nomeArquivo: arquivoSelecionado.name, dataUpload: dataUpload.value }))
      } catch {
        // Evita restaurar uma base antiga quando a nova excede a cota da sessão.
        try { sessionStorage.removeItem(CHAVE) } catch { /* Segue em memória. */ }
      }
      return true
    } catch (e) {
      if (atual !== operacao) return false
      erro.value = e.message ?? 'Não foi possível ler a planilha.'
      return false
    } finally {
      if (atual === operacao) carregando.value = false
    }
  }

  function limpar() {
    operacao++
    arquivo.value = null
    dadosOriginais.value = []
    carregando.value = false
    dataUpload.value = null
    periodoFaturamento.value = 'não informado'
    totalColunas.value = 0
    clientes.value = []
    erros.value = []
    resumo.value = { ...RESUMO_VAZIO }
    nomeArquivo.value = ''
    erro.value = ''
    try {
      sessionStorage.removeItem(CHAVE)
    } catch {
      // sem persistência disponível, nada a limpar
    }
  }

  const totalLinhas = computed(() => clientes.value.length)
  const linhasValidas = computed(() => clientes.value.filter(c => c.valida))
  const linhasComErro = computed(() => clientes.value.filter(c => !c.valida))
  const totalValidas = computed(() => linhasValidas.value.length)
  const totalComErro = computed(() => linhasComErro.value.length)
  const dadosTratados = computed(() => clientes.value)
  const totalClientes = computed(() => totalValidas.value)
  const totalErros = computed(() => erros.value.length)
  const clientesNivelA = computed(() => linhasValidas.value.filter(c => c.nivel === 'A').length)

  return {
    arquivo,
    dadosOriginais,
    dadosTratados,
    totalClientes,
    totalErros,
    clientesNivelA,
    selecionarArquivo,
    validarArquivo,
    processarPlanilha,
    periodoFaturamento,
    rotuloFaturamento,
    dataUpload,
    totalColunas,
    totalLinhas,
    linhasValidas,
    linhasComErro,
    totalValidas,
    totalComErro,
    clientes,
    clientesExibidos,
    clientesValidos,
    erros,
    errosPorTipo,
    resumo,
    nomeArquivo,
    carregando,
    erro,
    temDados,
    usandoExemplo,
    indicadores,
    faturamentoTotal,
    faturamentoPorSegmento,
    importarPlanilha,
    limpar
  }
})
