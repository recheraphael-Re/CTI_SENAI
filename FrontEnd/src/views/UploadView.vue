<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUploadStore } from '../stores/upload.js'
import { LIMITE_MB } from '../utils/planilha.js'

const store = useUploadStore()
const router = useRouter()
const arrastando = ref(false)

async function escolher(arquivo) {
  if (arquivo && await store.importarPlanilha(arquivo)) {
    await router.push({ name: 'relatorio' })
  }
}

function selecionar(evento) {
  const arquivo = evento.target.files[0]
  evento.target.value = ''
  escolher(arquivo)
}

function soltar(evento) {
  arrastando.value = false
  escolher(evento.dataTransfer.files[0])
}
</script>

<template>
  <section aria-labelledby="upload-titulo">
    <h1 id="upload-titulo" class="text-3xl font-bold text-brand dark:text-brand-light">Enviar planilha</h1>
    <p class="mt-2 text-slate-600 dark:text-slate-300">
      Selecione a planilha de clientes para revisar as linhas antes do envio.
    </p>
    <label
      class="mt-6 flex cursor-pointer flex-col items-center gap-3 rounded-xl border-2 border-dashed border-slate-300 bg-white p-8 text-center hover:border-brand focus-within:ring-2 focus-within:ring-brand sm:p-12 dark:border-slate-700 dark:bg-slate-900 dark:hover:border-brand-light"
      :class="{ 'ring-2 ring-brand': arrastando }"
      @dragover.prevent="arrastando = true"
      @dragleave="arrastando = false"
      @drop.prevent="soltar"
    >
      <input type="file" accept=".xlsx,.xls,.csv" class="sr-only" aria-label="Selecionar arquivo" aria-describedby="upload-formatos" :disabled="store.carregando || store.validandoBackend" @change="selecionar" />
      <span aria-hidden="true" class="text-3xl">☁️</span>
      <span class="font-semibold">Arraste e solte a planilha aqui</span>
      <span class="rounded-lg bg-linear-to-r from-brand to-brand-accent px-5 py-3 text-sm font-semibold text-white" aria-live="polite">
        {{ store.carregando ? 'Lendo planilha…' : 'Selecionar arquivo' }}
      </span>
      <span id="upload-formatos" class="text-xs text-slate-600 dark:text-slate-300">XLSX, XLS ou CSV de até {{ LIMITE_MB }} MB</span>
    </label>
    <p v-if="store.erro" role="alert" class="mt-4 rounded-lg bg-rose-50 p-3 text-rose-700 dark:bg-rose-950 dark:text-rose-300">{{ store.erro }}</p>
    <RouterLink v-if="store.temDados" :to="{ name: 'relatorio' }" class="mt-5 inline-block font-semibold text-brand hover:underline dark:text-brand-light">
      Voltar à revisão de {{ store.nomeArquivo }}
    </RouterLink>
  </section>
</template>
