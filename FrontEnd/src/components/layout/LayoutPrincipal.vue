<template>
  <!-- APPLAYOUT — estrutura fixa do site: header + conteúdo (slot) + footer -->
  <div class="min-h-screen flex flex-col">
    <CabecalhoPrincipal />

    <!-- slot: cada página injeta seu conteúdo aqui -->
    <main class="flex-1 max-w-6xl w-full mx-auto p-6 md:p-10">
      <slot />
    </main>

    <RodapePrincipal />
  </div>
</template>

<script setup>
import CabecalhoPrincipal from './CabecalhoPrincipal.vue'
import RodapePrincipal from './RodapePrincipal.vue'
</script>
