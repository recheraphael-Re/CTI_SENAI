<template>
  <!-- O layout é fixo; o router-view troca só o conteúdo da página -->
  <LayoutPrincipal>
    <RouterView />
  </LayoutPrincipal>
</template>

<script setup>
import { RouterView } from 'vue-router'
import LayoutPrincipal from './components/layout/LayoutPrincipal.vue'
</script>
