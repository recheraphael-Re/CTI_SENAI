<template>
  <!-- CARTÃOINDICADOR — um número com rótulo. Usado nos KPIs dos Gráficos
       (dentro de uma caixa) e no resumo da importação do Upload (solto). -->
  <div :class="classeDaCaixa">
    <p class="text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">
      {{ rotulo }}
    </p>
    <p class="mt-2 font-bold" :class="[classeDoTamanho, classeDoTom]">
      {{ valor }}
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const CAIXA =
  'rounded-xl border border-slate-200 bg-white p-6 text-center dark:border-slate-800 dark:bg-slate-900'

const TAMANHOS = {
  grande: 'text-3xl',
  medio: 'text-2xl'
}

// O tom é escolhido por nome, para as telas não precisarem repetir
// as classes do Tailwind e o dark: de cada cor.
const TONS = {
  marca: 'bg-gradient-to-r from-brand to-brand-accent dark:from-brand-light dark:to-brand-accent-light bg-clip-text text-transparent',
  neutro: 'text-slate-900 dark:text-slate-100',
  ok: 'text-emerald-600 dark:text-emerald-400',
  erro: 'text-rose-600 dark:text-rose-400',
  aviso: 'text-amber-600 dark:text-amber-400'
}

// Os validadores repetem os nomes porque o defineProps é içado para fora
// da setup() e não enxerga as constantes acima.
const props = defineProps({
  rotulo: { type: String, required: true },
  valor: { type: [String, Number], required: true },
  tom: {
    type: String,
    default: 'marca',
    validator: (valor) => ['marca', 'neutro', 'ok', 'erro', 'aviso'].includes(valor)
  },
  tamanho: {
    type: String,
    default: 'grande',
    validator: (valor) => ['grande', 'medio'].includes(valor)
  },
  // Desenha a moldura em volta. No Upload os cartões já ficam dentro
  // de uma caixa maior, então lá vai como falso.
  caixa: { type: Boolean, default: true }
})

const classeDaCaixa = computed(() => (props.caixa ? CAIXA : ''))
const classeDoTamanho = computed(() => TAMANHOS[props.tamanho])
const classeDoTom = computed(() => TONS[props.tom])
</script>
