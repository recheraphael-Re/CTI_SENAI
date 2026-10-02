<template>
  <!-- UPLOAD — envio da planilha Excel para tratamento -->
  <section>
    <p class="text-slate-600 dark:text-slate-400 mb-6">
      Envie a planilha de clientes para tratamento e análise.
      A leitura acontece no seu próprio navegador.
    </p>

    <!-- Área de arrastar e soltar -->
    <div
      class="rounded-xl border-2 border-dashed border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 p-10 text-center transition"
      :class="{ 'border-brand': arrastando }"
      @dragover.prevent="arrastando = true"
      @dragleave.prevent="arrastando = false"
      @drop.prevent="soltar"
    >
      <p class="text-4xl">☁️</p>
      <p class="mt-3 text-slate-700 dark:text-slate-300 font-semibold">
        Arraste e solte sua planilha aqui
      </p>
      <p class="text-sm text-slate-500 dark:text-slate-400">ou</p>

      <label class="mt-4 inline-block cursor-pointer bg-gradient-to-r from-brand to-brand-accent text-white px-6 py-3 rounded-lg hover:opacity-90 transition font-semibold">
        Selecionar arquivo
        <input type="file" accept=".xlsx,.xls,.csv" class="hidden" :disabled="carregando" @change="selecionar" />
      </label>

      <p class="mt-3 text-xs text-slate-500 dark:text-slate-400">
        Formatos aceitos: .xlsx, .xls, .csv
      </p>

      <p v-if="arquivo" class="mt-4 text-sm font-semibold text-brand dark:text-brand-light">
        Arquivo selecionado: {{ arquivo.name }}
      </p>
    </div>

    <!-- Erro que impediu a leitura (arquivo inválido, sem cabeçalho...) -->
    <p
      v-if="erro"
      class="mt-4 rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-500/30 dark:bg-rose-500/10 dark:text-rose-400"
    >
      {{ erro }}
    </p>

    <div class="mt-6 flex flex-wrap items-center gap-4">
      <button
        :disabled="!arquivo || carregando"
        class="bg-slate-900 dark:bg-slate-100 text-white dark:text-slate-900 px-6 py-3 rounded-lg font-semibold transition disabled:opacity-40 disabled:cursor-not-allowed"
        @click="processar"
      >
        {{ carregando ? 'Lendo planilha...' : 'Enviar e processar' }}
      </button>

      <button
        v-if="temDados"
        class="text-sm text-slate-600 underline hover:text-brand dark:text-slate-400 dark:hover:text-brand-light"
        @click="limpar"
      >
        Descartar planilha
      </button>
    </div>

    <!-- RESUMO DA IMPORTAÇÃO — o que o tratamento encontrou na planilha -->
    <div
      v-if="temDados"
      class="mt-8 rounded-xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900"
    >
      <h3 class="text-lg font-bold text-slate-900 dark:text-slate-100">
        Resumo da importação
      </h3>
      <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
        {{ nomeArquivo }} — aba <strong class="font-semibold">{{ resumo.aba }}</strong>
      </p>

      <div class="mt-5 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <CartaoIndicador
          v-for="cartao in cartoes"
          :key="cartao.rotulo"
          :rotulo="cartao.rotulo"
          :valor="cartao.valor"
          :tom="cartao.tom"
          tamanho="medio"
          :caixa="false"
        />
      </div>

      <!-- Ocorrências agrupadas por tipo, como o gabarito da planilha de teste -->
      <div v-if="errosPorTipo.length" class="mt-6">
        <p class="text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">
          Ocorrências por tipo
        </p>
        <ul class="mt-2 flex flex-wrap gap-2">
          <li
            v-for="item in errosPorTipo"
            :key="item.tipo"
            class="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-800 dark:bg-amber-500/15 dark:text-amber-400"
          >
            {{ item.quantidade }}× {{ item.tipo.replaceAll('_', ' ').toLowerCase() }}
          </li>
        </ul>
        <p class="mt-3 text-sm text-slate-600 dark:text-slate-400">
          As linhas com erro ficam de fora dos indicadores e aparecem marcadas nos
          <RouterLink to="/app/relatorios" class="font-semibold underline">relatórios</RouterLink>.
        </p>
      </div>

      <div class="mt-6">
        <h3 class="font-bold text-slate-900 dark:text-slate-100">Prévia dos clientes tratados</h3>
        <p class="mt-2 text-sm text-slate-600 dark:text-slate-400">
          {{ totalClientes }} clientes válidos · {{ clientesNivelA }} clientes válidos de nível A.
          Primeiras {{ previa.length }} de {{ resumo.linhas }} linhas importadas.
        </p>
        <div class="mt-3 overflow-x-auto">
          <table class="w-full text-left text-sm text-slate-700 dark:text-slate-300">
            <caption class="sr-only">Prévia da planilha após padronização; linhas com erro estão identificadas.</caption>
            <thead class="bg-slate-100 dark:bg-slate-800">
              <tr>
                <th scope="col" class="p-3">Linha</th>
                <th scope="col" class="p-3">Código</th>
                <th scope="col" class="p-3">Cliente</th>
                <th scope="col" class="p-3">Consultor</th>
                <th scope="col" class="p-3">Segmento</th>
                <th scope="col" class="p-3">Nível</th>
                <th scope="col" class="p-3">Validação</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cliente in previa" :key="cliente.linha" class="border-b border-slate-200 dark:border-slate-800">
                <td class="p-3">{{ cliente.linha }}</td>
                <td class="p-3">{{ cliente.id || '—' }}</td>
                <td class="p-3">{{ cliente.empresa || '—' }}</td>
                <td class="p-3">{{ cliente.consultor || '—' }}</td>
                <td class="p-3">{{ cliente.segmento }}</td>
                <td class="p-3">{{ cliente.nivel }}</td>
                <td class="p-3" :class="cliente.valida ? 'text-emerald-700 dark:text-emerald-400' : 'text-rose-700 dark:text-rose-400'">
                  {{ cliente.valida ? 'Válido' : cliente.erros.map(e => e.descricao).join('; ') }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="mt-6 flex flex-wrap gap-3">
        <RouterLink
          to="/app/graficos"
          class="rounded-lg bg-gradient-to-r from-brand to-brand-accent px-5 py-2.5 font-semibold text-white transition hover:opacity-90"
        >
          Ver gráficos
        </RouterLink>
        <RouterLink
          to="/app/relatorios"
          class="rounded-lg border border-slate-300 px-5 py-2.5 font-semibold text-slate-700 transition hover:bg-slate-100 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
        >
          Ver relatórios
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore.js'
import CartaoIndicador from '../components/CartaoIndicador.vue'

const store = useUploadStore()
const { arquivo, nomeArquivo, carregando, erro, temDados, resumo, errosPorTipo, dadosTratados, totalClientes, clientesNivelA } = storeToRefs(store)
const previa = computed(() => dadosTratados.value.slice(0, 10))

function limpar() {
  store.limpar()
}

const arrastando = ref(false)

const cartoes = computed(() => [
  { rotulo: 'Linhas lidas', valor: resumo.value.linhas, tom: 'neutro' },
  { rotulo: 'Válidas', valor: resumo.value.validas, tom: 'ok' },
  { rotulo: 'Com erro', valor: resumo.value.comErro, tom: 'erro' },
  { rotulo: 'Ocorrências', valor: resumo.value.ocorrencias, tom: 'aviso' }
])

function selecionar(evento) {
  receberArquivo(evento.target.files[0])
  evento.target.value = ''
}

function soltar(evento) {
  arrastando.value = false
  receberArquivo(evento.dataTransfer.files[0])
}

function receberArquivo(novo) {
  store.selecionarArquivo(novo)
}

// Lê a planilha e mostra o resumo aqui mesmo, em vez de pular direto para
// os gráficos: o que o tratamento encontrou é parte do resultado.
async function processar() {
  await store.processarPlanilha()
}
</script>
