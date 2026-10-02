 <template>
  <!-- HOME — tela de apresentação (landing page) -->

  <!-- Hero -->
  <section id="inicio" class="py-10 md:py-16 text-center">
    <span class="inline-block bg-brand/10 dark:bg-brand/15 text-brand dark:text-brand-light text-xs font-semibold px-3 py-1 rounded-full mb-4">
      🚀 Projeto Integrador 2026
    </span>

    <h2 class="text-3xl md:text-5xl font-bold text-slate-900 dark:text-slate-100 mb-4 leading-tight">
      Transforme sua planilha em
      <span class="bg-gradient-to-r from-brand to-brand-accent dark:from-brand-light dark:to-brand-accent-light bg-clip-text text-transparent">
        dados estratégicos
      </span>
    </h2>

    <p class="text-slate-600 dark:text-slate-400 max-w-2xl mx-auto mb-6 text-lg">
      Organize, analise e gere insights da sua carteira de clientes com base em dados reais.
    </p>

    <div class="flex flex-col sm:flex-row gap-3 justify-center">
      <RouterLink
        to="/login"
        class="bg-gradient-to-r from-brand to-brand-accent text-white px-6 py-3 rounded-lg hover:opacity-90 transition shadow-lg hover:shadow-brand/30 font-semibold"
      >
        Acessar sistema
      </RouterLink>
      <a
        href="#servicos"
        class="bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 px-6 py-3 rounded-lg hover:border-brand-light hover:text-brand dark:hover:text-brand-light transition font-semibold"
      >
        Saiba mais
      </a>
    </div>
  </section>

  <!-- Grid de serviços -->
  <section id="servicos" class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-10">
    <ServiceCard
      v-for="servico in servicos"
      :key="servico.titulo"
      :titulo="servico.titulo"
      :descricao="servico.descricao"
      :icone="servico.icone"
    />
  </section>

  <!-- Imagem do produto — passo 06 da aula (object-cover não deforma a foto) -->
  <section id="sobre" class="mt-16 grid gap-10 lg:grid-cols-2 lg:items-center">
    <div>
      <p class="text-sm font-bold uppercase text-brand dark:text-brand-light">Sobre o sistema</p>
      <h3 class="mt-2 text-2xl md:text-3xl font-bold text-slate-900 dark:text-slate-100">
        Do Excel solto ao painel de decisão.
      </h3>
      <p class="mt-3 text-slate-600 dark:text-slate-400 text-lg">
        O CTI Insights lê a planilha da carteira de clientes, padroniza segmentos e
        níveis (A, B e C) e devolve tudo em indicadores prontos para a reunião comercial.
      </p>
    </div>

    <!-- relative: base de posição para o detalhe decorativo -->
    <div class="relative max-w-lg mx-auto lg:mx-0">
      <!-- absolute: quadrado da marca atrás da foto -->
      <div class="absolute -left-3 -top-3 h-20 w-20 rounded-lg bg-brand"></div>
      <!-- z-10 na frente | object-cover para a imagem não esticar -->
      <img
        :src="imagemPainel"
        alt="Demonstração do painel do CTI Insights"
        class="relative z-10 aspect-[4/3] w-full rounded-xl object-cover shadow-xl"
      />
    </div>
  </section>

  <!-- Gráfico -->
  <section class="mt-16">
    <p class="mb-3 text-sm text-slate-500">Demonstração com dados fictícios.</p>
    <RevenueChart />
  </section>

  <!-- Banner com foto de fundo — passo 07 da aula -->
  <section class="relative isolate mt-16 overflow-hidden rounded-2xl bg-slate-900 py-20 px-6 text-center">
    <!-- inset-0 -z-10: a foto preenche o bloco e fica atrás do texto -->
    <!-- opacity-30: escurece a foto para o texto branco continuar legível -->
    <img
      :src="imagemPainel"
      alt=""
      aria-hidden="true"
      class="absolute inset-0 -z-10 h-full w-full object-cover opacity-30"
    />

    <div class="text-white">
      <h3 class="text-2xl md:text-4xl font-bold">
        Qual segmento traz mais retorno?
      </h3>
      <p class="mt-3 text-slate-300 max-w-xl mx-auto">
        Hoje essa resposta leva horas de planilha. Com o CTI Insights, leva um upload.
      </p>
      <RouterLink
        to="/app/upload"
        class="mt-6 inline-block bg-brand px-6 py-3 rounded-lg font-semibold hover:opacity-90 transition"
      >
        Enviar minha planilha
      </RouterLink>
    </div>
  </section>

  <!-- Contato -->
  <section id="contato" class="mt-16 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-8 md:p-10">
    <div class="grid gap-8 md:grid-cols-2 md:items-center">
      <div>
        <p class="text-sm font-bold uppercase text-brand dark:text-brand-light">Contato</p>
        <h3 class="mt-2 text-2xl font-bold text-slate-900 dark:text-slate-100">
          Fale com a equipe do projeto.
        </h3>
        <p class="mt-3 text-slate-600 dark:text-slate-400">
          Projeto Integrador II 2026/02 — SENAI Roberto Mange, em parceria com a
          CTI Comunicação de Dados e Tecnologia.
        </p>
      </div>

      <form class="flex flex-col gap-3" @submit.prevent="enviarContato">
        <input
          v-model="contato.nome"
          type="text"
          placeholder="Seu nome"
          class="rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 outline-none focus:border-brand"
        />
        <input
          v-model="contato.email"
          type="email"
          placeholder="Seu e-mail"
          class="rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 outline-none focus:border-brand"
        />
        <textarea
          v-model="contato.mensagem"
          rows="3"
          placeholder="Como podemos ajudar?"
          class="rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 px-4 py-2 outline-none focus:border-brand"
        ></textarea>
        <button
          type="submit"
          class="bg-gradient-to-r from-brand to-brand-accent text-white px-6 py-3 rounded-lg hover:opacity-90 transition font-semibold"
        >
          Enviar mensagem
        </button>
        <p v-if="enviado" class="text-sm font-semibold text-brand dark:text-brand-light">
          Formulário demonstrativo: nenhuma mensagem foi enviada.
        </p>
      </form>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import ServiceCard from '../components/ServiceCard.vue'
import RevenueChart from '../components/RevenueChart.vue'
import imagemPainel from '../assets/hero.png'

const servicos = ref([
  {
    titulo: 'Limpeza de Dados',
    descricao: 'Padronização e tratamento de planilhas Excel para análise confiável.',
    icone: '🧹'
  },
  {
    titulo: 'Análise Estratégica',
    descricao: 'Identificação de padrões e oportunidades comerciais na carteira.',
    icone: '📈'
  },
  {
    titulo: 'Dashboards',
    descricao: 'Visualização clara de faturamento, segmentos e perfil de clientes.',
    icone: '📊'
  }
])

// Formulário de contato — ainda sem back-end, apenas confirma o envio na tela
const contato = reactive({ nome: '', email: '', mensagem: '' })
const enviado = ref(false)

function enviarContato() {
  enviado.value = true
}
</script>
