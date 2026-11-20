<script setup>
defineProps({ status: { type: String, required: true } })
const cores = {
  PROCESSADO: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-500/15 dark:text-emerald-300',
  PROCESSANDO: 'bg-amber-100 text-amber-800 dark:bg-amber-500/15 dark:text-amber-300',
  ERRO: 'bg-rose-100 text-rose-800 dark:bg-rose-500/15 dark:text-rose-300'
}
</script>

<template>
  <span class="inline-block rounded-full px-2.5 py-1 text-xs font-semibold"
    :class="cores[status] ?? 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300'">
    {{ status }}
  </span>
</template>
