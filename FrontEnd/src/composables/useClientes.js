// ESTADO DA CARTEIRA DE CLIENTES — compartilhado por Upload, Relatórios e Gráficos.
// Mesmo padrão do useTheme: o ref fica FORA da função, então existe um só
// para o app inteiro; quem chama useClientes() enxerga sempre a mesma lista.
import { ref, computed } from 'vue'
import { lerPlanilha } from '../utils/planilha.js'
import { formatarMoeda } from '../utils/formato.js'

const CHAVE = 'cti-insights-clientes'

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

const sessao = lerDaSessao()
const clientes = ref(sessao?.clientes ?? [])
const erros = ref(sessao?.erros ?? [])
const resumo = ref(sessao?.resumo ?? { ...RESUMO_VAZIO })
const nomeArquivo = ref(sessao?.nomeArquivo ?? '')
const carregando = ref(false)
const erro = ref('')

export function useClientes() {
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
      { rotulo: 'Faturamento mensal médio', valor: formatarMoeda(media) },
      { rotulo: 'Contratos ativos', valor: ativos.toLocaleString('pt-BR') }
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

  // Chamado pelo UploadView quando o usuário confirma o envio
  async function importarPlanilha(arquivo) {
    carregando.value = true
    erro.value = ''

    try {
      const lido = await lerPlanilha(arquivo)
      clientes.value = lido.clientes
      erros.value = lido.erros
      resumo.value = lido.resumo
      nomeArquivo.value = arquivo.name

      try {
        sessionStorage.setItem(CHAVE, JSON.stringify({ ...lido, nomeArquivo: arquivo.name }))
      } catch {
        // Planilha grande demais para a sessão: segue só em memória
      }
      return true
    } catch (e) {
      erro.value = e.message ?? 'Não foi possível ler a planilha.'
      return false
    } finally {
      carregando.value = false
    }
  }

  function limpar() {
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

  return {
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
}
