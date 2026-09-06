// FORMATAÇÃO E NÚMEROS — funções puras, sem dependência do SheetJS.
// Ficam separadas para o gráfico da Home poder formatar valores sem
// carregar a biblioteca de leitura de planilha junto.

// Deixa o texto comparável: sem acento, sem espaço, tudo minúsculo.
// "Faixa de Faturamento Anual" vira "faixadefaturamentoanual".
export function normalizar(texto) {
  return String(texto ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '')
}

// Converte um pedaço de texto em número: "R$ 1.200.000,00", "850 mil", "1,2M".
function numeroSimples(texto) {
  const limpo = String(texto)
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/r\$|\s/g, '')

  const encontrado = limpo.match(/[\d.,]+/)
  if (!encontrado) return null

  let numero = encontrado[0]
  if (numero.includes(',')) {
    numero = numero.replace(/\./g, '').replace(',', '.') // 1.200.000,50 -> 1200000.50
  } else if (/\.\d{3}(\.|$)/.test(numero)) {
    numero = numero.replace(/\./g, '') // 1.200.000 -> 1200000
  }

  const valor = parseFloat(numero)
  if (!Number.isFinite(valor)) return null

  // Um "-" colado antes do número indica valor negativo, que precisa
  // chegar negativo até a validação (a base de teste tem valores assim).
  const sinal = limpo.slice(0, encontrado.index).endsWith('-') ? -1 : 1

  // O que vem depois do número indica a escala: 1,2M / 850 mil / 2 milhões
  const sufixo = limpo.slice(encontrado.index + encontrado[0].length)
  if (/^milh/.test(sufixo)) return sinal * valor * 1e6
  if (/^mil/.test(sufixo)) return sinal * valor * 1e3
  if (/^k/.test(sufixo)) return sinal * valor * 1e3
  if (/^m/.test(sufixo)) return sinal * valor * 1e6
  return sinal * valor
}

// Interpreta o faturamento anual. Quando a célula traz uma FAIXA
// ("de 500 mil a 1 milhão"), usa o ponto médio para poder somar e comparar.
export function interpretarNumero(valor) {
  if (typeof valor === 'number') return Number.isFinite(valor) ? valor : 0

  const texto = String(valor ?? '').trim()
  if (!texto) return 0

  // Marca o hífen como separador só quando há número dos dois lados,
  // para não confundir com o sinal de um valor negativo.
  const marcado = texto.replace(/(\d\s*[kKmM]?)\s*[-–—]\s*(?=[R$\s]*\d)/g, '$1 a ')

  const partes = marcado
    .split(/\s+(?:a|ate|até)\s+/i)
    .map(numeroSimples)
    .filter((n) => n !== null)

  if (partes.length === 0) return 0
  return partes.reduce((soma, n) => soma + n, 0) / partes.length
}

// Formata para exibição, na escala que o valor pedir.
export function formatarMoeda(valor) {
  const numero = Number(valor) || 0
  if (numero < 0) return `-${formatarMoeda(-numero)}`
  if (numero >= 1e6) {
    return `R$ ${(numero / 1e6).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mi`
  }
  if (numero >= 1e3) {
    return `R$ ${Math.round(numero / 1e3).toLocaleString('pt-BR')} mil`
  }
  return `R$ ${numero.toLocaleString('pt-BR', { maximumFractionDigits: 0 })}`
}
