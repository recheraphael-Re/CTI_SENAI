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
  { id: 1001, empresa: 'TechCorp S.A.', consultor: 'Ana', segmento: 'Tecnologia', faturamento: 1200000, faturamentoOriginal: '', nivel: 'A', servicos: ['Link dedicado', 'Cloud'], ativo: true },
  { id: 1002, empresa: 'Indústrias ABC', consultor: 'Bruno', segmento: 'Manufatura', faturamento: 850000, faturamentoOriginal: '', nivel: 'B', servicos: ['Link dedicado'], ativo: true },
  { id: 1003, empresa: 'Comercial Silva', consultor: 'Ana', segmento: 'Varejo', faturamento: 200000, faturamentoOriginal: '', nivel: 'C', servicos: ['Banda larga'], ativo: false },
  { id: 1004, empresa: 'Logística Express', consultor: 'Carla', segmento: 'Transporte', faturamento: 3500000, faturamentoOriginal: '', nivel: 'A', servicos: ['Link dedicado', 'VPN', 'Cloud'], ativo: true },
  { id: 1005, empresa: 'Serviços Gerais Ltda', consultor: 'Bruno', segmento: 'Serviços', faturamento: 420000, faturamentoOriginal: '', nivel: 'B', servicos: ['Banda larga', 'VPN'], ativo: true }
]

// Recupera o último envio, para um F5 não apagar a planilha da sessão
function lerDaSessao() {
  try {
    const salvo = JSON.parse(sessionStorage.getItem(CHAVE) ?? 'null')
    return salvo?.clientes?.length ? salvo : { clientes: [], nomeArquivo: '' }
  } catch {
    return { clientes: [], nomeArquivo: '' }
  }
}

const sessao = lerDaSessao()
const clientes = ref(sessao.clientes)
const nomeArquivo = ref(sessao.nomeArquivo)
const carregando = ref(false)
const erro = ref('')

export function useClientes() {
  // Há planilha carregada? Senão, as telas caem nos dados de exemplo.
  const temDados = computed(() => clientes.value.length > 0)
  const usandoExemplo = computed(() => !temDados.value)
  const clientesExibidos = computed(() => (temDados.value ? clientes.value : CLIENTES_EXEMPLO))

  const faturamentoTotal = computed(() =>
    clientesExibidos.value.reduce((soma, c) => soma + c.faturamento, 0)
  )

  const indicadores = computed(() => {
    const lista = clientesExibidos.value
    const media = lista.length ? faturamentoTotal.value / lista.length : 0
    const servicos = lista.reduce((soma, c) => soma + c.servicos.length, 0)

    return [
      { rotulo: 'Total de clientes', valor: lista.length.toLocaleString('pt-BR') },
      { rotulo: 'Faturamento médio', valor: formatarMoeda(media) },
      { rotulo: 'Serviços ativos', valor: servicos.toLocaleString('pt-BR') }
    ]
  })

  // Soma o faturamento por segmento e devolve pronto para o gráfico de barras
  const faturamentoPorSegmento = computed(() => {
    const soma = new Map()
    for (const cliente of clientesExibidos.value) {
      soma.set(cliente.segmento, (soma.get(cliente.segmento) ?? 0) + cliente.faturamento)
    }

    const ordenado = [...soma.entries()].sort((a, b) => b[1] - a[1]).slice(0, 8)
    return {
      rotulos: ordenado.map(([segmento]) => segmento),
      valores: ordenado.map(([, valor]) => valor)
    }
  })

  // Chamado pelo UploadView quando o usuário confirma o envio
  async function importarPlanilha(arquivo) {
    carregando.value = true
    erro.value = ''

    try {
      const lidos = await lerPlanilha(arquivo)
      clientes.value = lidos
      nomeArquivo.value = arquivo.name

      try {
        sessionStorage.setItem(CHAVE, JSON.stringify({ clientes: lidos, nomeArquivo: arquivo.name }))
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
