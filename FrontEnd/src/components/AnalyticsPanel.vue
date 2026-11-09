<template>
  <section class="space-y-6" aria-label="Análises salvas no banco">
    <div class="flex flex-wrap items-center gap-3">
      <label for="lote-analytics">Importação salva</label>
      <select id="lote-analytics" v-model="selecionado" :disabled="store.carregando" @change="carregar"
        class="max-w-full rounded border p-2 dark:bg-slate-800">
        <option value="">Último lote processado</option>
        <option v-for="lote in store.lotes" :key="lote.id" :value="lote.id">
          #{{ lote.id }} — {{ lote.arquivoNome }} — {{ lote.status }}
        </option>
      </select>
      <button class="rounded border px-4 py-2 disabled:opacity-40" :disabled="store.carregando" @click="carregar">Atualizar</button>
    </div>
    <p v-if="store.carregando" role="status">Consultando importações no servidor...</p>
    <p v-else-if="store.erro" role="alert" class="rounded border border-rose-300 p-4 text-rose-700 dark:text-rose-300">{{ store.erro }}</p>
    <p v-else-if="!store.resultado" class="rounded border p-4">Nenhuma importação processada no banco. Leia uma planilha e clique em “Processar e salvar no banco” na tela de upload.</p>
    <template v-else>
      <p class="text-sm text-slate-600 dark:text-slate-300">
        Fonte: PostgreSQL · lote #{{ lote.id }} · {{ lote.arquivoNome }} ·
        {{ lote.linhasValidas }} clientes válidos · {{ lote.linhasInvalidas }} linhas descartadas.
        Os lotes são independentes; seus valores não são somados.
      </p>
      <template v-if="!relatorio">
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
          <CartaoIndicador v-for="item in indicadores" :key="item.rotulo" v-bind="item" />
        </div>
        <RevenueChart titulo="Faturamento anual dos clientes por segmento" :rotulos="faturamento.rotulos" :valores="faturamento.valores" />
        <div class="grid min-w-0 gap-6 lg:grid-cols-2">
          <GraficoContagem titulo="Clientes por segmento" descricao="Clientes válidos do lote." :dados="serie('porSegmento', 'segmento', 'clientes')" />
          <GraficoContagem titulo="Clientes por nível" descricao="Distribuição de A, B e C." :dados="serie('porNivel', 'nivel', 'clientes')" />
          <GraficoContagem titulo="Clientes por mês de contratação" descricao="Meses entre a primeira e a última contratação; intervalos sem registros aparecem com zero." :dados="serie('porMes', 'mes', 'clientes')" linha />
          <GraficoContagem titulo="Serviços contratados" descricao="Um cliente pode contratar mais de um serviço." :dados="serie('porServico', 'servico', 'contratos')" />
        </div>
        <section class="rounded-xl border p-5 dark:border-slate-700">
          <h3 class="text-lg font-bold">Análise e insights do lote</h3>
          <p class="mt-3">Desvio padrão amostral: {{ moeda(stats.resumo.desvioPadrao) }}. Quartis: {{ moeda(stats.resumo.q1) }} e {{ moeda(stats.resumo.q3) }}.</p>
          <p class="mt-3">{{ stats.correlacao?.interpretacao || 'Dados insuficientes ou constantes para calcular a correlação.' }}</p>
          <p v-if="stats.correlacao?.pearson != null" class="mt-2">Pearson r = {{ stats.correlacao.pearson }}; p = {{ stats.correlacao.p }}; n = {{ stats.correlacao.n }}. Associação não comprova causa.</p>
          <ul class="mt-3 list-disc space-y-2 pl-5"><li v-for="(texto, i) in stats.insights" :key="i">{{ texto }}</li></ul>
          <p class="mt-4 text-sm text-slate-500 dark:text-slate-400">Faturamento das empresas clientes, não receita da CTI. Os resultados descrevem este lote e não comprovam tendências futuras.</p>
        </section>
      </template>
      <section v-else>
        <label class="block">Buscar no lote
          <input v-model="busca" type="search" placeholder="Código, empresa, segmento, consultor ou serviço"
            class="mt-2 w-full rounded border p-3 dark:bg-slate-800" />
        </label>
        <p class="my-3 text-sm">{{ filtrados.length }} contratos exibidos. Um cliente aparece uma vez por serviço contratado; faturamento não deve ser somado por linha.</p>
        <div class="overflow-x-auto rounded-xl border dark:border-slate-700">
          <table class="w-full text-left text-sm">
            <caption class="sr-only">Clientes e serviços persistidos no lote selecionado</caption>
            <thead><tr><th v-for="titulo in ['Código', 'Empresa', 'Consultor', 'Segmento', 'Nível', 'Faturamento anual', 'Serviço', 'Contratação']" :key="titulo" scope="col" class="p-3">{{ titulo }}</th></tr></thead>
            <tbody><tr v-for="(c, i) in filtrados" :key="i" class="border-t dark:border-slate-700">
              <td class="p-3">{{ c.codigo }}</td><td class="p-3">{{ c.nome }}</td><td class="p-3">{{ c.consultor }}</td>
              <td class="p-3">{{ c.segmento }}</td><td class="p-3">{{ c.nivel }}</td><td class="p-3">{{ moeda(c.faturamento) }}</td>
              <td class="p-3">{{ c.servico }}</td><td class="p-3">{{ c.dataContratacao }}</td>
            </tr><tr v-if="!filtrados.length"><td colspan="8" class="p-4">Nenhum contrato encontrado.</td></tr></tbody>
          </table>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useDashboardStore } from '../stores/dashboard.js'
import CartaoIndicador from './CartaoIndicador.vue'
import RevenueChart from './RevenueChart.vue'
import GraficoContagem from './GraficoContagem.vue'
import { formatarMoeda } from '../utils/formato.js'

defineProps({ relatorio: Boolean })
const store = useDashboardStore()
const selecionado = ref('')
const busca = ref('')
const lote = computed(() => store.resultado?.lote)
const stats = computed(() => store.resultado?.estatisticas)
const moeda = valor => valor == null ? 'Não disponível' : formatarMoeda(valor)
const indicadores = computed(() => stats.value ? [
  { rotulo: 'Clientes', valor: String(stats.value.resumo.totalClientes) },
  ...[['Faturamento anual total', 'faturamentoTotal'], ['Média', 'media'], ['Mediana', 'mediana']]
    .map(([rotulo, chave]) => ({ rotulo, valor: moeda(stats.value.resumo[chave]) })),
  { rotulo: 'Consultores', valor: String(stats.value.resumo.consultores) }
] : [])
function serie(chave, nome, valor) {
  const itens = stats.value?.[chave] || []
  return { rotulos: itens.map(i => i[nome]), valores: itens.map(i => i[valor]) }
}
const faturamento = computed(() => serie('porSegmento', 'segmento', 'faturamentoTotal'))
const filtrados = computed(() => {
  const termo = busca.value.trim().toLocaleLowerCase('pt-BR')
  return store.clientes.filter(c => [c.codigo, c.nome, c.consultor, c.segmento, c.servico]
    .some(v => String(v || '').toLocaleLowerCase('pt-BR').includes(termo)))
})
function carregar() { return store.carregar(selecionado.value || null) }
onMounted(carregar)
</script>
