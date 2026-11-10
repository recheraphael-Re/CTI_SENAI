import { defineStore } from 'pinia'
import { ref } from 'vue'
import api, { mensagemErro } from '../services/api.js'

export const useDashboardStore = defineStore('dashboard', () => {
  const lotes = ref([])
  const resultado = ref(null)
  const clientes = ref([])
  const carregando = ref(false)
  const erro = ref('')
  let sequencia = 0

  function limpar() {
    sequencia++
    lotes.value = []; resultado.value = null; clientes.value = []
    erro.value = ''; carregando.value = false
  }

  async function carregar(loteId = null) {
    const atual = ++sequencia
    carregando.value = true; erro.value = ''; resultado.value = null; clientes.value = []
    try {
      const historico = await api.get('/api/dashboard/lotes')
      if (atual !== sequencia) return
      lotes.value = historico.data
      const id = loteId || lotes.value.find(l => l.status === 'PROCESSADO')?.id || lotes.value[0]?.id
      if (!id) return
      const analise = await api.get('/api/dashboard', { params: { loteId: id } })
      if (atual !== sequencia) return
      const registros = analise.data.estatisticas ? await api.get(`/api/dashboard/lotes/${id}/clientes`) : { data: [] }
      if (atual !== sequencia) return
      resultado.value = analise.data; clientes.value = registros.data
    } catch (e) {
      if (atual === sequencia) erro.value = mensagemErro(e)
    } finally {
      if (atual === sequencia) carregando.value = false
    }
  }
  return { lotes, resultado, clientes, carregando, erro, carregar, limpar }
})
