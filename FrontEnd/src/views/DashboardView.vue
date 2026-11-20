<template>
  <section class="analytics-page space-y-6" aria-labelledby="dashboard-titulo">
    <header class="analytics-heading">
      <h1 id="dashboard-titulo" class="text-3xl font-bold text-brand dark:text-brand-light">Dashboard</h1>
      <p>Consulte os indicadores e os contratos de cada importação.</p>
    </header>
    <div class="analytics-toolbar">
      <label for="lote-analytics">Importação salva</label>
      <select id="lote-analytics" :value="selecionado" :disabled="store.carregando" @change="selecionarLote"
        class="max-w-full rounded border p-2 dark:bg-slate-800">
        <option value="">Último lote processado</option>
        <option v-for="lote in store.lotes" :key="lote.id" :value="String(lote.id)">
          #{{ lote.id }} — {{ lote.arquivoNome }} — {{ lote.status }}
        </option>
      </select>
      <button class="analytics-refresh disabled:opacity-40" :disabled="store.carregando" @click="carregar">Atualizar</button>
    </div>
    <p v-if="store.carregando" role="status">Consultando importações no servidor...</p>
    <p v-else-if="store.erro" role="alert" class="rounded border border-rose-300 p-4 text-rose-700 dark:text-rose-300">{{ store.erro }}</p>
    <section v-else-if="!store.resultado" class="analytics-empty">
      <div class="analytics-empty-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M4 19h16M7 15v-4m5 4V5m5 10V8" stroke-linecap="round" /></svg>
      </div>
      <h3>Sua próxima análise começa com uma planilha</h3>
      <p>Envie uma planilha e confirme o processamento na tela de revisão.</p>
      <RouterLink :to="{ name: 'upload' }" class="analytics-refresh">Enviar planilha</RouterLink>
    </section>
    <template v-else>
      <p class="text-sm text-slate-600 dark:text-slate-300">
        Lote #{{ lote.id }} · {{ lote.arquivoNome }} · {{ lote.linhasValidas }} clientes válidos ·
        {{ lote.linhasInvalidas }} linhas descartadas.
        <StatusBadge :status="lote.status" class="ml-2" />
      </p>
      <p v-if="lote.status === 'ERRO'" role="alert" class="rounded border border-rose-300 p-4 text-rose-700 dark:text-rose-300">Falha no lote: {{ lote.mensagem || 'Consulte o log do serviço Python.' }}</p>
      <p v-else-if="!stats" role="status" class="rounded border p-4">{{ lote.status === 'PROCESSANDO' ? 'O Python está processando este lote. A consulta será atualizada automaticamente.' : 'Este lote ainda não possui estatísticas disponíveis.' }}</p>
      <template v-if="stats">
        <p class="text-sm text-slate-500 dark:text-slate-400">Análise gerada em {{ dataHora(store.resultado.geradoEm || stats.geradoEm) }} · {{ stats.resumo.consultores }} consultores.</p>
        <div class="analytics-metrics grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <KpiCard v-for="item in indicadores" :key="item.rotulo" v-bind="item" />
        </div>
        <BaseChart titulo="Faturamento anual por segmento" :dados="faturamento" moeda />
        <BaseChart titulo="Faturamento anual por consultor" :dados="consultores" moeda />
        <div class="grid min-w-0 gap-6 lg:grid-cols-2">
          <BaseChart titulo="Clientes por segmento" descricao="Clientes válidos do lote." :dados="serie('porSegmento', 'segmento', 'clientes')" />
          <BaseChart titulo="Clientes por nível" descricao="Distribuição de A, B e C." :dados="serie('porNivel', 'nivel', 'clientes')" />
          <BaseChart titulo="Clientes por mês de contratação" descricao="Meses entre a primeira e a última contratação; intervalos sem registros aparecem com zero." :dados="serie('porMes', 'mes', 'clientes')" linha />
          <BaseChart titulo="Serviços contratados" descricao="Um cliente pode contratar mais de um serviço." :dados="serie('porServico', 'servico', 'contratos')" />
        </div>
        <section v-if="stats.topClientes?.length" class="analytics-card overflow-x-auto rounded-xl border p-5 dark:border-slate-700">
          <h3 class="mb-3 text-lg font-bold">Cinco maiores clientes por faturamento anual</h3>
          <table class="w-full text-left text-sm">
            <thead><tr><th v-for="titulo in ['Código', 'Cliente', 'Segmento', 'Nível', 'Faturamento anual']" :key="titulo" scope="col" class="p-3">{{ titulo }}</th></tr></thead>
            <tbody><tr v-for="c in stats.topClientes" :key="c.codigo" class="border-t dark:border-slate-700">
              <td class="p-3">{{ c.codigo }}</td><td class="p-3">{{ c.nome }}</td><td class="p-3">{{ c.segmento }}</td><td class="p-3">{{ c.nivel }}</td><td class="p-3">{{ moeda(c.faturamento) }}</td>
            </tr></tbody>
          </table>
        </section>
        <section class="analytics-card rounded-xl border p-5 dark:border-slate-700">
          <h3 class="text-lg font-bold">Análise e insights do lote</h3>
          <p class="mt-3">Desvio padrão amostral: {{ moeda(stats.resumo.desvioPadrao) }}. Quartis: {{ moeda(stats.resumo.q1) }} e {{ moeda(stats.resumo.q3) }}.</p>
          <p class="mt-3">{{ stats.correlacao?.interpretacao || 'Dados insuficientes ou constantes para calcular a correlação.' }}</p>
          <p v-if="stats.correlacao?.pearson != null" class="mt-2">Pearson r = {{ stats.correlacao.pearson }}; p = {{ stats.correlacao.p }}; n = {{ stats.correlacao.n }}. Associação não comprova causa.</p>
          <ul class="mt-3 list-disc space-y-2 pl-5"><li v-for="(texto, i) in stats.insights" :key="i">{{ texto }}</li></ul>
          <p class="mt-4 text-sm text-slate-500 dark:text-slate-400">Faturamento das empresas clientes, não receita da CTI. Os resultados descrevem este lote e não comprovam tendências futuras.</p>
        </section>
        <details class="analytics-card rounded-xl border p-5">
          <summary class="cursor-pointer">Contratos do lote</summary>
          <label class="mt-4 block">Buscar no lote
              <input v-model="busca" type="search" placeholder="Código, empresa, segmento, consultor ou serviço"
                class="mt-2 w-full rounded border p-3 dark:bg-slate-800" />
            </label>
            <p class="my-3 text-sm">{{ filtrados.length }} contratos exibidos. Um cliente aparece uma vez por serviço contratado; faturamento não deve ser somado por linha.</p>
            <div class="analytics-table overflow-x-auto rounded-xl border dark:border-slate-700">
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
        </details>
      </template>
    </template>
  </section>
</template>

<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDashboardStore } from '../stores/dashboard.js'
import KpiCard from '../components/KpiCard.vue'
import BaseChart from '../components/BaseChart.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { formatarMoeda } from '../utils/formato.js'

const store = useDashboardStore()
const route = useRoute()
const router = useRouter()
const selecionado = computed(() => typeof route.query.lote === 'string' && /^[1-9]\d*$/.test(route.query.lote) ? route.query.lote : '')

function selecionarLote(evento) {
  const lote = evento.target.value
  router.replace({ name: 'dashboard', query: lote ? { lote } : {} })
}
const busca = ref('')
const lote = computed(() => store.resultado?.lote)
const stats = computed(() => store.resultado?.estatisticas)
const moeda = valor => valor == null ? 'Não disponível' : formatarMoeda(valor)
const indicadores = computed(() => stats.value ? [
  { rotulo: 'Clientes', valor: String(stats.value.resumo.totalClientes) },
  ...[['Faturamento anual total', 'faturamentoTotal'], ['Média', 'media'], ['Mediana', 'mediana']]
    .map(([rotulo, chave]) => ({ rotulo, valor: moeda(stats.value.resumo[chave]) })),
  { rotulo: 'Desvio padrão amostral', valor: moeda(stats.value.resumo.desvioPadrao) }
] : [])
function serie(chave, nome, valor) {
  const itens = stats.value?.[chave] || []
  return { rotulos: itens.map(i => i[nome]), valores: itens.map(i => i[valor]) }
}
const faturamento = computed(() => serie('porSegmento', 'segmento', 'faturamentoTotal'))
const consultores = computed(() => serie('porConsultor', 'consultor', 'faturamentoTotal'))
const dataHora = valor => valor ? new Date(valor).toLocaleString('pt-BR') : 'Não informada'
const filtrados = computed(() => {
  const termo = busca.value.trim().toLocaleLowerCase('pt-BR')
  return store.clientes.filter(c => [c.codigo, c.nome, c.consultor, c.segmento, c.servico]
    .some(v => String(v || '').toLocaleLowerCase('pt-BR').includes(termo)))
})
let relogio
let ativo = true
async function carregar() {
  clearTimeout(relogio)
  await store.carregar(selecionado.value || null)
  if (ativo && store.resultado?.lote.status === 'PROCESSANDO') relogio = setTimeout(carregar, 5000)
}
watch(selecionado, carregar, { immediate: true })
onBeforeUnmount(() => { ativo = false; clearTimeout(relogio) })
</script>
