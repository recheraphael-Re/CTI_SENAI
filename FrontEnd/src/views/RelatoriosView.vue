<template>
  <!-- RELATÓRIOS — consulta da base de clientes tratada -->
  <section>
    <div class="mb-6 flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
      <p class="text-slate-600 dark:text-slate-400">
        Base de clientes detalhada, já padronizada pelo tratamento.
      </p>

      <input
        v-model="busca"
        type="search"
        placeholder="Buscar empresa..."
        class="w-full rounded-lg border border-slate-300 bg-white px-4 py-2 outline-none focus:border-brand sm:w-64 dark:border-slate-700 dark:bg-slate-800"
      />
    </div>

    <!-- Deixa claro quando a tela ainda não tem planilha de verdade -->
    <p
      v-if="usandoExemplo"
      class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-400"
    >
      Estes são dados de exemplo.
      <RouterLink to="/app/upload" class="font-semibold underline">Envie uma planilha</RouterLink>
      para ver a sua carteira.
    </p>

    <!-- overflow-x-auto: a tabela rola sozinha em telas estreitas -->
    <div class="overflow-x-auto rounded-xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
      <table class="w-full text-left text-sm">
        <thead class="border-b border-slate-200 text-xs uppercase text-slate-500 dark:border-slate-800 dark:text-slate-400">
          <tr>
            <th class="px-4 py-3">ID</th>
            <th class="px-4 py-3">Empresa</th>
            <th class="px-4 py-3">Segmento</th>
            <th class="px-4 py-3">Faturamento anual</th>
            <th class="px-4 py-3">Nível</th>
            <th class="px-4 py-3">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(cliente, indice) in clientesFiltrados"
            :key="`${cliente.id}-${indice}`"
            class="border-b border-slate-100 last:border-0 hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50"
          >
            <td class="px-4 py-3 text-slate-500">{{ cliente.id }}</td>
            <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{{ cliente.empresa }}</td>
            <td class="px-4 py-3">{{ cliente.segmento }}</td>
            <!-- title mostra como o valor estava escrito na planilha original -->
            <td class="px-4 py-3" :title="cliente.faturamentoOriginal">
              {{ formatarMoeda(cliente.faturamento) }}
            </td>
            <td class="px-4 py-3 font-bold">{{ cliente.nivel }}</td>
            <td class="px-4 py-3">
              <span
                class="rounded-full px-2 py-1 text-xs font-semibold"
                :class="cliente.ativo
                  ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-400'
                  : 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-400'"
              >
                {{ cliente.ativo ? 'Ativo' : 'Inativo' }}
              </span>
            </td>
          </tr>

          <tr v-if="clientesFiltrados.length === 0">
            <td colspan="6" class="px-4 py-8 text-center text-slate-500">
              Nenhuma empresa encontrada para "{{ busca }}".
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useClientes } from '../composables/useClientes.js'
import { formatarMoeda } from '../utils/formato.js'

const busca = ref('')

// Vem da planilha enviada no Upload; sem planilha, cai nos dados de exemplo
const { clientesExibidos, usandoExemplo } = useClientes()

// Filtra pela busca digitada, ignorando maiúsculas e minúsculas
const clientesFiltrados = computed(() => {
  const termo = busca.value.trim().toLowerCase()
  if (!termo) return clientesExibidos.value

  return clientesExibidos.value.filter((c) =>
    c.empresa.toLowerCase().includes(termo) ||
    c.segmento.toLowerCase().includes(termo) ||
    c.consultor.toLowerCase().includes(termo)
  )
})
</script>
