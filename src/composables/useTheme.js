import { ref, watch, onMounted } from 'vue'

// Estado global do tema (compartilhado entre componentes)
const isDark = ref(false)

export function useTheme() {
  onMounted(() => {
    // 1. Verifica se há preferência salva
    const saved = localStorage.getItem('theme')

    if (saved) {
      isDark.value = saved === 'dark'
    } else {
      // 2. Senão, detecta a preferência do sistema
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }

    // Aplica a classe no <html>
    applyTheme()
  })

  // Observa mudanças e persiste
  watch(isDark, () => {
    applyTheme()
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  })

  function applyTheme() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  function toggleTheme() {
    isDark.value = !isDark.value
  }

  return { isDark, toggleTheme }
}
