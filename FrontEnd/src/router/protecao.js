export function criarProtecao(auth) {
  return async to => {
    if (!to.meta.requiresAuth) return
    try { await auth.restaurar() }
    catch { return { name: 'login', query: { motivo: 'sessao' } } }
    if (!auth.logado) return { name: 'login', query: { redirect: to.fullPath } }
    if (to.meta.admin && !auth.isAdmin) return { name: 'graficos' }
  }
}
