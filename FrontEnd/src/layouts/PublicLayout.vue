<template>
  <!-- PUBLICLAYOUT — moldura das páginas públicas (Home e Login) -->
  <div class="flex min-h-screen flex-col">
    <SiteHeader />

    <!-- Área dinâmica das telas públicas -->
    <main class="mx-auto w-full max-w-6xl flex-1 p-6 md:p-10">
      <RouterView />
    </main>

    <SiteFooter />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import SiteHeader from '../components/SiteHeader.vue'
import SiteFooter from '../components/SiteFooter.vue'
</script>
