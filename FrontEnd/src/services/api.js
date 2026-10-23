import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env?.VITE_API_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// Configurado pelo store, evitando dependência circular com o roteador.
let interceptores = []
export function configurarAutenticacao(obterToken, sessaoInvalida) {
  if (interceptores.length) {
    api.interceptors.request.eject(interceptores[0])
    api.interceptors.response.eject(interceptores[1])
  }
  const pedido = api.interceptors.request.use(config => {
    if (!['/auth/login', '/auth/register'].includes(config.url)) {
      const token = obterToken()
      if (token) config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })
  const resposta = api.interceptors.response.use(resposta => resposta, erro => {
    if (erro.response?.status === 401 && !['/auth/login', '/auth/register'].includes(erro.config?.url)) sessaoInvalida()
    return Promise.reject(erro)
  })
  interceptores = [pedido, resposta]
}

export function mensagemErro(erro) {
  if (!erro.response && erro.isAxiosError) return 'Não foi possível conectar ao servidor. Tente novamente mais tarde.'
  const codigo = erro.response?.data?.code
  if (codigo === 'USUARIO_PENDENTE') return 'Sua solicitação aguarda aprovação do administrador.'
  if (codigo === 'USUARIO_BLOQUEADO') return 'Seu acesso está bloqueado. Procure o administrador.'
  return erro.response?.data?.message || erro.message || 'Não foi possível concluir a operação.'
}
export default api
