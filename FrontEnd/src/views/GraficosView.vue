<template>
  <!-- GRÁFICOS — KPIs e indicadores da carteira -->
  <section>
    <p class="text-slate-600 dark:text-slate-400 mb-6">
      Visão geral dos principais indicadores e insights.
    </p>

    <!-- Deixa claro quando a tela ainda não tem planilha de verdade -->
    <p
      v-if="usandoExemplo"
      class="mb-6 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-400"
    >
      Estes são dados de exemplo.
      <RouterLink to="/app/upload" class="font-semibold underline">Envie uma planilha</RouterLink>
      para ver a sua carteira.
    </p>
    <p v-else class="mb-6 text-sm text-slate-500 dark:text-slate-400">
      Fonte: <strong class="font-semibold">{{ nomeArquivo }}</strong> —
      {{ resumo.validas }} linhas válidas somam
      {{ formatarMoeda(faturamentoTotal) }}. {{ store.rotuloFaturamento }}.
      <template v-if="resumo.comErro">
        {{ resumo.comErro }} linhas ficaram de fora por erro de validação.
      </template>
    </p>

    <div class="mb-6 flex flex-wrap gap-4">
      <label>Segmento
        <select v-model="segmento" class="ml-2 rounded border p-2 dark:bg-slate-800">
          <option value="">Todos</option>
          <option v-for="nome in segmentos" :key="nome">{{ nome }}</option>
        </select>
      </label>
      <label>Nível
        <select v-model="nivel" class="ml-2 rounded border p-2 dark:bg-slate-800">
          <option value="">Todos</option>
          <option v-for="nome in niveis" :key="nome">{{ nome }}</option>
        </select>
      </label>
    </div>
    <p class="mb-4 text-sm">Indicadores gerais da base (os filtros abaixo se aplicam aos gráficos de contagem e à análise).</p>
    <!-- Cards de indicadores -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
      <CartaoIndicador
        v-for="kpi in indicadores"
        :key="kpi.rotulo"
        :rotulo="kpi.rotulo"
        :valor="kpi.valor"
      />
    </div>

    <!-- Gráfico de faturamento, somado por segmento -->
    <div class="mt-8">
      <RevenueChart
        :titulo="`${store.rotuloFaturamento} por segmento — base completa`"
        :rotulos="faturamentoPorSegmento.rotulos"
        :valores="faturamentoPorSegmento.valores"
      />
    </div>
    <div class="mt-8 grid min-w-0 gap-6 lg:grid-cols-2">
      <GraficoContagem titulo="Clientes por segmento" descricao="Quantidade de registros válidos por segmento." :dados="porSegmento" />
      <GraficoContagem titulo="Clientes por nível" descricao="Distribuição dos níveis na seleção atual." :dados="porNivel" />
      <GraficoContagem titulo="Evolução das contratações" :descricao="`Registros por mês de início, independentemente do status atual. ${semData} registros sem data foram excluídos. Somente meses observados.`" :dados="porMes" linha />
      <GraficoContagem titulo="Serviços na carteira" descricao="Frequência dos serviços nos registros válidos selecionados." :dados="porServico" />
    </div>
    <section class="mt-8 rounded-xl border border-slate-200 p-5 dark:border-slate-700">
      <h3 class="text-lg font-bold">Análise descritiva da seleção</h3>
      <p v-if="!estatistica" class="mt-3">Nenhum registro válido corresponde aos filtros.</p>
      <template v-else>
        <p class="mt-3">{{ filtrados.length }} clientes. Média: {{ formatarMoeda(estatistica.media) }};
          mediana: {{ formatarMoeda(estatistica.mediana) }}; desvio padrão populacional: {{ formatarMoeda(estatistica.desvio) }}.</p>
        <p v-if="porSegmento.rotulos.length" class="mt-3">Maior concentração: {{ porSegmento.rotulos[0] }}, com {{ porSegmento.valores[0] }} clientes ({{ (porSegmento.valores[0] / filtrados.length * 100).toFixed(1) }}%). Em caso de empate, é exibido o primeiro em ordem alfabética.</p>
      </template>
      <p class="mt-3 text-sm text-slate-500 dark:text-slate-400">{{ store.rotuloFaturamento }} das empresas clientes, não receita da CTI. Faixas são estimadas pelo ponto médio. Os resultados descrevem a base importada; não comprovam causas nem tendências futuras.</p>
    </section>
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import GraficoContagem from '../components/GraficoContagem.vue'
import { contar, evolucao, estatisticas } from '../utils/analise.js'
import { RouterLink } from 'vue-router'
import RevenueChart from '../components/RevenueChart.vue'
import CartaoIndicador from '../components/CartaoIndicador.vue'
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore.js'
import { formatarMoeda } from '../utils/formato.js'

// Os indicadores saem da planilha enviada no Upload; sem planilha, da amostra
const store = useUploadStore()
const segmento = ref('')
const nivel = ref('')
watch(() => store.clientes, () => { segmento.value = ''; nivel.value = '' })
const segmentos = computed(() => [...new Set(store.clientesValidos.map(c => c.segmento))].sort())
const niveis = computed(() => [...new Set(store.clientesValidos.map(c => c.nivel))].sort())
const filtrados = computed(() => store.clientesValidos.filter(c => (!segmento.value || c.segmento === segmento.value) && (!nivel.value || c.nivel === nivel.value)))
const porSegmento = computed(() => contar(filtrados.value, 'segmento'))
const porNivel = computed(() => contar(filtrados.value, 'nivel'))
const porServico = computed(() => contar(filtrados.value, 'servico'))
const porMes = computed(() => evolucao(filtrados.value))
const semData = computed(() => filtrados.value.filter(c => !c.dataInicio).length)
const estatistica = computed(() => estatisticas(filtrados.value))
const { indicadores, faturamentoPorSegmento, faturamentoTotal, usandoExemplo, nomeArquivo, resumo } =
  storeToRefs(store)
</script>
