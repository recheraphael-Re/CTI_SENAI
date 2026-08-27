import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

// Rotas das 4 telas do sistema CTI Insights
const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/upload', name: 'upload', component: () => import('../views/UploadView.vue') },
  { path: '/dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue') }
]

export default createRouter({
  history: createWebHistory(),
  routes,
  // Sempre abre a página no topo ao trocar de rota
  scrollBehavior: () => ({ top: 0 })
})
