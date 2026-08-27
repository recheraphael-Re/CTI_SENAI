<template>
  <div class="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl p-6 hover:border-brand-light dark:hover:border-brand hover:shadow-lg transition-all duration-300 cursor-pointer group">
    <div class="w-12 h-12 rounded-lg bg-gradient-to-br from-brand to-brand-accent flex items-center justify-center text-2xl mb-4 group-hover:scale-110 transition-transform">
      {{ icone }}
    </div>
    <h3 class="text-xl font-bold text-slate-900 dark:text-slate-100 mb-2">{{ titulo }}</h3>
    <p class="text-slate-600 dark:text-slate-400 text-sm leading-relaxed">{{ descricao }}</p>
  </div>
</template>

<script setup>
defineProps({
  titulo: { type: String, required: true },
  descricao: { type: String, required: true },
  icone: { type: String, default: '📊' }
})
</script>
