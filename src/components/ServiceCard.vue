<template>
  <div class="bg-white border border-slate-200 rounded-xl p-6 hover:border-violet-400 hover:shadow-lg transition-all duration-300 cursor-pointer group">
    <!-- Ícone com fundo gradiente -->
    <div class="w-12 h-12 rounded-lg bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center text-2xl mb-4 group-hover:scale-110 transition-transform">
      {{ icone }}
    </div>

    <!-- Título -->
    <h3 class="text-xl font-bold text-slate-900 mb-2">{{ titulo }}</h3>

    <!-- Descrição -->
    <p class="text-slate-600 text-sm leading-relaxed">{{ descricao }}</p>
  </div>
</template>

<script setup>
defineProps({
  titulo: {
    type: String,
    required: true
  },
  descricao: {
    type: String,
    required: true
  },
  icone: {
    type: String,
    default: '📊'
  }
})
</script>
