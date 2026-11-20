export function criarProtecao(auth) {
  return async to => {
    try {
      await auth.restaurar()
    } catch {
      if (to.meta.requiresAuth) return { name: 'login', query: { motivo: 'sessao' } }
    }
    if (to.meta.guestOnly && auth.logado) return { name: 'upload' }
    if (!to.meta.requiresAuth) return
    if (!auth.logado) return { name: 'login', query: { redirect: to.fullPath } }
    if (to.meta.admin && !auth.isAdmin) return { name: 'dashboard' }
  }
}
