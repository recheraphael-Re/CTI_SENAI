// Análise descritiva da base importada; não infere receita da CTI.
export function contar(lista, campo) {
  const grupos = new Map()
  for (const item of lista) grupos.set(item[campo], (grupos.get(item[campo]) ?? 0) + 1)
  const pares = [...grupos].sort((a, b) => b[1] - a[1] || String(a[0]).localeCompare(String(b[0])))
  return { rotulos: pares.map(([nome]) => nome), valores: pares.map(([, total]) => total) }
}

export function evolucao(lista) {
  const meses = new Map()
  for (const item of lista) {
    if (!item.dataInicio) continue
    const data = new Date(item.dataInicio)
    if (!Number.isFinite(data.getTime())) continue
    const chave = data.toISOString().slice(0, 7)
    meses.set(chave, (meses.get(chave) ?? 0) + 1)
  }
  // Somente meses observados: não inventar histórico onde não há dados.
  const pares = [...meses].sort(([a], [b]) => a.localeCompare(b))
  return { rotulos: pares.map(([mes]) => mes), valores: pares.map(([, total]) => total) }
}

export function estatisticas(lista) {
  const valores = lista.map(c => c.faturamento).filter(Number.isFinite).sort((a, b) => a - b)
  if (!valores.length) return null
  const media = valores.reduce((a, b) => a + b, 0) / valores.length
  const meio = Math.floor(valores.length / 2)
  const mediana = valores.length % 2 ? valores[meio] : (valores[meio - 1] + valores[meio]) / 2
  const desvio = Math.sqrt(valores.reduce((s, v) => s + (v - media) ** 2, 0) / valores.length)
  return { media, mediana, desvio }
}
