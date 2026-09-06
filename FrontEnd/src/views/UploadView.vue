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
        <input type="file" accept=".xlsx,.xls,.csv" class="hidden" @change="selecionar" />
      </label>

      <p class="mt-3 text-xs text-slate-500 dark:text-slate-400">
        Formatos aceitos: .xlsx, .xls, .csv
      </p>

      <p v-if="arquivo" class="mt-4 text-sm font-semibold text-brand dark:text-brand-light">
        Arquivo selecionado: {{ arquivo.name }}
      </p>
    </div>

    <!-- Erro devolvido pela leitura (cabeçalho não encontrado, arquivo inválido...) -->
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

      <!-- Resumo do último envio, que sobrevive à troca de tela -->
      <p v-if="temDados" class="text-sm text-slate-600 dark:text-slate-400">
        {{ clientes.length }} clientes carregados de
        <strong class="font-semibold">{{ nomeArquivo }}</strong>.
        <button class="ml-1 underline hover:text-brand dark:hover:text-brand-light" @click="limpar">
          Descartar
        </button>
      </p>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useClientes } from '../composables/useClientes.js'

const router = useRouter()
const { clientes, nomeArquivo, carregando, erro, temDados, importarPlanilha, limpar } = useClientes()

const arquivo = ref(null)
const arrastando = ref(false)

function selecionar(evento) {
  arquivo.value = evento.target.files[0] ?? null
  erro.value = ''
}

function soltar(evento) {
  arrastando.value = false
  arquivo.value = evento.dataTransfer.files[0] ?? null
  erro.value = ''
}

// Lê a planilha e, dando certo, leva o usuário direto aos gráficos
async function processar() {
  const deuCerto = await importarPlanilha(arquivo.value)
  if (deuCerto) router.push('/app/graficos')
}
</script>
