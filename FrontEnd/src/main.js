import { modoDemonstracao } from './utils/modoAcesso.js'
import { useAuthStore } from './stores/auth.js'
import { createApp, watch } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'


const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')

// Retira o painel da tela também quando uma requisição invalida a sessão.
const auth = useAuthStore()
watch(() => auth.token, token => {
  if (!modoDemonstracao && !token && router.currentRoute.value.meta.requiresAuth) {
    router.replace({ name: 'login', query: { motivo: 'sessao' } })
  }
})
