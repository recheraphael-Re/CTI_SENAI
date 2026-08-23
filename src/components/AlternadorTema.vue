<template>
  <button
    @click="toggleTheme"
    class="relative w-14 h-8 rounded-full bg-slate-200 dark:bg-slate-700 transition-colors duration-300 focus:outline-none focus:ring-2 focus:ring-brand"
    aria-label="Alternar tema"
  >
    <!-- Bolinha que desliza -->
    <span
      class="absolute top-1 left-1 w-6 h-6 rounded-full bg-white dark:bg-slate-900 shadow-md flex items-center justify-center transition-all duration-300"
      :class="{ 'translate-x-6': isDark }"
    >
      <!-- Sol -->
      <svg
        v-if="!isDark"
        xmlns="http://www.w3.org/2000/svg"
        class="w-4 h-4 text-amber-500"
        viewBox="0 0 24 24"
        fill="currentColor"
      >
        <path d="M12 2.25a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0V3a.75.75 0 01.75-.75zM7.5 9.75a.75.75 0 00-1.5 0v1.5a.75.75 0 001.5 0v-1.5zM12 7.5a4.5 4.5 0 100 9 4.5 4.5 0 000-9zM18.894 6.106a.75.75 0 00-1.06-1.06l-1.06 1.06a.75.75 0 101.06 1.06l1.06-1.06zM21.75 12a.75.75 0 01-.75.75h-1.5a.75.75 0 010-1.5H21a.75.75 0 01.75.75zM17.834 18.894a.75.75 0 001.06-1.06l-1.06-1.06a.75.75 0 10-1.06 1.06l1.06 1.06zM12 18.75a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5a.75.75 0 01.75-.75zM6.166 17.834a.75.75 0 00-1.06 1.06l1.06 1.06a.75.75 0 101.06-1.06l-1.06-1.06zM5.25 12a.75.75 0 01-.75.75H3a.75.75 0 010-1.5h1.5a.75.75 0 01.75.75zM7.22 5.106a.75.75 0 00-1.06 1.06l1.06 1.06a.75.75 0 001.06-1.06L7.22 5.106z"/>
      </svg>

      <!-- Lua -->
      <svg
        v-else
        xmlns="http://www.w3.org/2000/svg"
        class="w-4 h-4 text-brand-light"
        viewBox="0 0 24 24"
        fill="currentColor"
      >
        <path fill-rule="evenodd" d="M9.528 1.718a.75.75 0 01.162.819A12.036 12.036 0 0018.35 18.35a.75.75 0 01.819.162 12.036 12.036 0 01-14.36 2.066.75.75 0 01-.38-1.015 9.54 9.54 0 001.243-3.27 9.54 9.54 0 00-2.066-14.36.75.75 0 011.015-.38A12.036 12.036 0 009.528 1.718z" clip-rule="evenodd"/>
      </svg>
    </span>
  </button>
</template>

<script setup>
import { useTheme } from '../composables/useTheme.js'

const { isDark, toggleTheme } = useTheme()
</script>
