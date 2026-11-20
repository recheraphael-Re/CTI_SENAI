import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { criarProtecao } from './protecao.js'

const requiresAuth = { requiresAuth: true }
const guestOnly = { guestOnly: true }

const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: guestOnly },
  { path: '/cadastro', name: 'cadastro', component: () => import('../views/CadastroView.vue'), meta: guestOnly },
  { path: '/upload', name: 'upload', component: () => import('../views/UploadView.vue'), meta: requiresAuth },
  { path: '/relatorio', name: 'relatorio', component: () => import('../views/RelatorioView.vue'), meta: requiresAuth },
  { path: '/dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: requiresAuth },
  { path: '/admin/usuarios', name: 'admin', component: () => import('../views/AdminUsuariosView.vue'), meta: { ...requiresAuth, admin: true } },
  { path: '/app', redirect: { name: 'upload' } },
  { path: '/app/upload', redirect: { name: 'upload' } },
  { path: '/app/relatorios', redirect: { name: 'relatorio' } },
  { path: '/app/graficos', redirect: { name: 'dashboard' } },
  { path: '/app/usuarios', redirect: { name: 'admin' } },
  { path: '/:pathMatch(.*)*', name: 'nao-encontrada', component: () => import('../views/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(to => criarProtecao(useAuthStore())(to))

export default router
