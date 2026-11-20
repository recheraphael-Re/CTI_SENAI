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
  <section class="mx-auto max-w-2xl" aria-labelledby="upload-titulo">
    <h1 id="upload-titulo" class="text-3xl font-bold text-brand dark:text-brand-light">Enviar planilha</h1>
    <p class="mt-2 text-slate-600 dark:text-slate-300">
      Selecione um arquivo XLSX, XLS ou CSV de até {{ LIMITE_MB }} MB. Você poderá revisar as linhas antes do envio.
    </p>
    <label
      class="mt-6 block cursor-pointer rounded-xl border-2 border-dashed border-brand/60 bg-white p-8 text-center focus-within:ring-2 focus-within:ring-brand sm:p-12 dark:bg-slate-900"
      :class="{ 'ring-2 ring-brand': arrastando }"
      @dragover.prevent="arrastando = true"
      @dragleave="arrastando = false"
      @drop.prevent="soltar"
    >
      <input type="file" accept=".xlsx,.xls,.csv" class="sr-only" :disabled="store.carregando || store.validandoBackend" @change="selecionar" />
      <span class="block font-semibold text-brand dark:text-brand-light">
        {{ store.carregando ? 'Lendo planilha…' : 'Arraste o arquivo aqui ou clique para escolher' }}
      </span>
    </label>
    <p v-if="store.erro" role="alert" class="mt-4 rounded-lg bg-rose-50 p-3 text-rose-700 dark:bg-rose-950 dark:text-rose-300">{{ store.erro }}</p>
    <RouterLink v-if="store.temDados" :to="{ name: 'relatorio' }" class="mt-5 inline-block font-semibold text-brand hover:underline dark:text-brand-light">
      Voltar à revisão de {{ store.nomeArquivo }}
    </RouterLink>
  </section>
</template>
