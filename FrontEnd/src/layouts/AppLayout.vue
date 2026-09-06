<template>
  <!-- APPLAYOUT — moldura do painel interno: Sidebar + Header + área dinâmica -->
  <div class="min-h-screen bg-slate-50 dark:bg-slate-950">
    <Sidebar :aberta="sidebarAberta" @navegar="fecharNoCelular" />

    <!-- Fundo escuro que fecha a barra ao tocar fora (só no celular) -->
    <div
      v-if="sidebarAberta"
      class="fixed inset-0 z-30 bg-black/40 md:hidden"
      @click="sidebarAberta = false"
    ></div>

    <!-- A margem acompanha a largura da barra no desktop -->
    <div class="transition-all duration-300" :class="sidebarAberta ? 'md:ml-60' : 'md:ml-16'">
      <Header :sidebar-aberta="sidebarAberta" @alternar-sidebar="sidebarAberta = !sidebarAberta" />

      <!-- Área dinâmica: aqui entram Upload, Relatórios e Gráficos -->
      <main class="p-4 md:p-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import Header from '../components/Header.vue'

// ESTADO: a barra lateral começa recolhida no celular e aberta no desktop
const sidebarAberta = ref(window.innerWidth >= 768)

function fecharNoCelular() {
  if (window.innerWidth < 768) sidebarAberta.value = false
}
</script>
