import { criarProtecao } from './protecao.js'
import { useAuthStore } from '../stores/auth.js'
import { createRouter, createWebHistory } from 'vue-router'

// Layouts
import PublicLayout from '@/layouts/PublicLayout.vue'
import AppLayout from '@/layouts/AppLayout.vue'

// View pública inicial (carregada junto, por ser a porta de entrada)
import HomeView from '@/views/HomeView.vue'

// Demais views carregadas sob demanda (lazy loading)
const LoginView = () => import('@/views/LoginView.vue')
const CadastroView = () => import('@/views/CadastroView.vue')
const UploadView = () => import('@/views/UploadView.vue')
const RelatoriosView = () => import('@/views/RelatoriosView.vue')
const GraficosView = () => import('@/views/GraficosView.vue')

const routes = [
  // --- Rotas públicas (site institucional) ---
  {
    path: '/',
    component: PublicLayout,
    children: [
      { path: '', name: 'home', component: HomeView },
      { path: 'login', name: 'login', component: LoginView },
      { path: 'cadastro', name: 'cadastro', component: CadastroView }
    ]
  },

  // --- Rotas internas (painel, com Sidebar e Header) ---
  {
    path: '/app',
    component: AppLayout,
    meta: { requiresAuth: true },
    redirect: '/app/upload', // Ao acessar /app, cai na tela principal interna
    children: [
      { path: 'usuarios', name: 'usuarios', component: () => import('@/views/UsuariosView.vue'), meta: { admin: true } },
      { path: 'upload', name: 'upload', component: UploadView },
      { path: 'relatorios', name: 'relatorios', component: RelatoriosView },
      { path: 'graficos', name: 'graficos', component: GraficosView }
    ]
  },

  // Redireciona qualquer rota inexistente para a home
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // Rola até a âncora quando houver (#sobre, #contato); senão, volta ao topo
  scrollBehavior: (to) => (to.hash ? { el: to.hash, behavior: 'smooth' } : { top: 0 })
})

router.beforeEach(to => criarProtecao(useAuthStore())(to))

export default router
