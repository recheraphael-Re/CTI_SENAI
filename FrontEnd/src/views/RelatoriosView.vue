<template>
  <!-- RELATÓRIOS — consulta da base de clientes tratada -->
  <section>
    <section v-if="store.temDados" class="mb-6 rounded-xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900">
      <h2 class="text-lg font-bold">Informações do processamento</h2>
      <dl class="mt-4 grid gap-4 sm:grid-cols-2">
        <div><dt class="text-sm text-slate-500">Arquivo</dt><dd class="break-all font-semibold">{{ store.nomeArquivo }}</dd></div>
        <div><dt class="text-sm text-slate-500">Data e hora do upload</dt><dd>{{ dataUploadFormatada }}</dd></div>
        <div><dt>Linhas carregadas</dt><dd class="text-2xl font-bold">{{ store.totalLinhas }}</dd></div>
        <div><dt>Colunas</dt><dd class="text-2xl font-bold">{{ store.totalColunas }}</dd></div>
        <div><dt>Registros válidos</dt><dd class="text-2xl font-bold text-emerald-600">{{ store.totalValidas }}</dd></div>
        <div><dt>Registros com erro</dt><dd class="text-2xl font-bold text-rose-600">{{ store.totalComErro }}</dd></div>
      </dl>
      <p class="mt-4 font-semibold" :class="store.totalComErro ? 'text-rose-600' : 'text-emerald-600'">
        {{ store.totalComErro ? 'Foram encontrados erros na planilha. Confira os problemas na tabela abaixo.' : 'Planilha validada sem erros.' }}
      </p>
    </section>
    <div class="mb-6 flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
      <p class="text-slate-600 dark:text-slate-400">
        Base de clientes detalhada, já padronizada pelo tratamento.
      </p>

      <input
        v-model="busca"
        type="search"
        placeholder="Buscar empresa, segmento ou consultor..."
        class="w-full rounded-lg border border-slate-300 bg-white px-4 py-2 outline-none focus:border-brand sm:w-72 dark:border-slate-700 dark:bg-slate-800"
      />
    </div>

    <!-- Deixa claro quando a tela ainda não tem planilha de verdade -->
    <p
      v-if="usandoExemplo"
      class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-400"
    >
      Nenhuma planilha carregada.
      <RouterLink to="/app/upload" class="font-semibold underline">Envie uma planilha</RouterLink>
      para gerar o relatório.
    </p>

    <!-- Filtro das linhas que o tratamento marcou -->
    <label
      v-else-if="resumo.comErro"
      class="mb-4 flex w-fit cursor-pointer items-center gap-2 text-sm text-slate-700 dark:text-slate-300"
    >
      <input v-model="somenteErros" type="checkbox" class="accent-brand" />
      Mostrar só as {{ resumo.comErro }} linhas com erro
    </label>

    <!-- overflow-x-auto: a tabela rola sozinha em telas estreitas -->
    <div v-if="store.temDados" class="overflow-x-auto rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
      <table class="w-full text-left text-sm">
        <thead class="border-b border-slate-200 text-xs uppercase text-slate-500 dark:border-slate-800 dark:text-slate-400">
          <tr>
            <th class="px-4 py-3">Linha</th>
            <th class="px-4 py-3">Código</th>
            <th class="px-4 py-3">Empresa</th>
            <th class="px-4 py-3">Consultor</th>
            <th class="px-4 py-3">Segmento</th>
            <th class="px-4 py-3">{{ store.rotuloFaturamento }}</th>
            <th class="px-4 py-3">Nível</th>
            <th class="px-4 py-3">Status</th>
            <th class="px-4 py-3">Problemas encontrados</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="cliente in clientesFiltrados"
            :key="cliente.linha"
            class="border-b border-slate-100 last:border-0 hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50"
            :class="{ 'bg-amber-50/60 dark:bg-amber-500/5': !cliente.valida }"
          >
            <td class="px-4 py-3">{{ cliente.linha }}</td>
            <td class="px-4 py-3 text-slate-500">
              <!-- O aviso lista os erros da linha, no mesmo vocabulário do gabarito -->
              <span
                v-if="!cliente.valida"
                class="mr-1 cursor-help text-amber-600 dark:text-amber-400"
                :title="descreverErros(cliente)"
              >⚠</span>
              {{ cliente.id || '—' }}
            </td>
            <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">
              {{ cliente.empresa || '—' }}
            </td>
            <td class="px-4 py-3">{{ cliente.consultor || '—' }}</td>
            <!-- title mostra a grafia original, antes da padronização -->
            <td class="px-4 py-3" :title="cliente.segmentoOriginal">{{ cliente.segmento }}</td>
            <td class="px-4 py-3" :class="{ 'text-rose-600 dark:text-rose-400': cliente.faturamento < 0 }">
              {{ cliente.faturamento === null ? 'Inválido ou ausente' : formatarMoeda(cliente.faturamento) }}
            </td>
            <td class="px-4 py-3 font-bold">{{ cliente.nivel }}</td>
            <td class="px-4 py-3">
              <span
                class="rounded-full px-2 py-1 text-xs font-semibold"
                :class="corDoStatus(cliente.status)"
                :title="cliente.statusOriginal"
              >
                {{ cliente.status }}
              </span>
            </td>
            <td class="min-w-64 px-4 py-3">
              <ul v-if="cliente.erros.length" class="list-disc space-y-1 pl-4 text-rose-600 dark:text-rose-400">
                <li v-for="(erro, indice) in cliente.erros" :key="indice">{{ erro.descricao }}</li>
              </ul>
              <span v-else class="text-emerald-600">Sem erros</span>
            </td>
          </tr>

          <tr v-if="clientesFiltrados.length === 0">
            <td colspan="9" class="px-4 py-8 text-center text-slate-500">
              Nenhuma empresa encontrada.
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-if="store.temDados" class="mt-3 text-sm text-slate-500 dark:text-slate-400">
      {{ clientesFiltrados.length }} de {{ clientesExibidos.length }} linhas.
    </p>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUploadStore } from '../stores/uploadStore.js'
import { formatarMoeda } from '../utils/formato.js'

const busca = ref('')
const somenteErros = ref(false)

// O relatório usa somente os registros importados na store compartilhada.
const store = useUploadStore()
const { clientes: clientesExibidos, usandoExemplo, resumo } = storeToRefs(store)
const dataUploadFormatada = computed(() => store.dataUpload
  ? new Date(store.dataUpload).toLocaleString('pt-BR') : 'Não informado')

const clientesFiltrados = computed(() => {
  const termo = busca.value.trim().toLowerCase()

  return clientesExibidos.value.filter((c) => {
    if (somenteErros.value && store.totalComErro > 0 && c.valida) return false
    if (!termo) return true

    return (
      c.empresa.toLowerCase().includes(termo) ||
      c.segmento.toLowerCase().includes(termo) ||
      c.consultor.toLowerCase().includes(termo) ||
      c.id.toLowerCase().includes(termo)
    )
  })
})

// Um texto por linha, para o balão do aviso
function descreverErros(cliente) {
  return cliente.erros.map((e) => `${e.tipo}: ${e.descricao}`).join('\n')
}

function corDoStatus(status) {
  if (status === 'ATIVO') return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-400'
  if (status === 'PENDENTE') return 'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-400'
  if (status === 'CANCELADO') return 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-400'
  // Fora dos três status válidos: fica neutro, e o aviso da linha explica
  return 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
}
</script>
