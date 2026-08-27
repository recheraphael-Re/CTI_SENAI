<template>
  <!-- UPLOAD — envio da planilha Excel para tratamento -->
  <section class="py-10">
    <h2 class="text-2xl md:text-3xl font-bold text-slate-900 dark:text-slate-100">
      Upload de Planilha
    </h2>
    <p class="text-slate-600 dark:text-slate-400 mt-1 mb-6">
      Envie a planilha de clientes para tratamento e análise.
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

    <button
      :disabled="!arquivo"
      class="mt-6 bg-slate-900 dark:bg-slate-100 text-white dark:text-slate-900 px-6 py-3 rounded-lg font-semibold transition disabled:opacity-40 disabled:cursor-not-allowed"
      @click="processar"
    >
      Enviar e processar
    </button>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const arquivo = ref(null)
const arrastando = ref(false)

function selecionar(evento) {
  arquivo.value = evento.target.files[0] ?? null
}

function soltar(evento) {
  arrastando.value = false
  arquivo.value = evento.dataTransfer.files[0] ?? null
}

// Sem back-end ainda: segue direto para o dashboard
function processar() {
  router.push('/dashboard')
}
</script>
