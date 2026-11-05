<template>
  <v-container class="py-8">
    <div class="d-flex align-center flex-wrap ga-4 mb-6">
      <div>
        <h1 class="text-h5 font-weight-bold">Indicadores</h1>
        <p class="text-body-2 text-medium-emphasis mb-0">
          <code>GET /funcionarios/indicadores</code>
        </p>
      </div>
      <v-spacer />
      <v-btn
        variant="text"
        class="text-none"
        prepend-icon="mdi-refresh"
        :loading="loading"
        @click="carregar"
      >
        Atualizar
      </v-btn>
    </div>

    <v-row>
      <v-col cols="12" sm="6" md="4">
        <v-card variant="flat" color="primary" class="pa-2">
          <v-card-item>
            <div class="text-caption text-uppercase">Total de candidatos</div>
            <div class="text-h3 font-weight-bold">{{ indicadores.total }}</div>
          </v-card-item>
        </v-card>
      </v-col>

      <v-col v-for="card in cards" :key="card.key" cols="12" sm="6" md="4">
        <v-card variant="outlined" class="pa-2 h-100">
          <v-card-item>
            <div class="d-flex align-center ga-2 mb-1">
              <v-icon :icon="card.icon" :color="card.cor" size="20" />
              <span class="text-caption text-uppercase text-medium-emphasis">
                {{ card.label }}
              </span>
            </div>
            <div class="d-flex align-end ga-3">
              <div class="text-h4 font-weight-bold">
                {{ indicadores[card.key] }}
              </div>
              <div class="text-body-2 text-medium-emphasis pb-1">
                {{ percentual(indicadores[card.key]) }}
              </div>
            </div>
            <v-progress-linear
              :model-value="proporcao(indicadores[card.key])"
              :color="card.cor"
              height="6"
              rounded
              class="mt-3"
            />
          </v-card-item>
        </v-card>
      </v-col>
    </v-row>

    <v-alert
      v-if="erro"
      type="error"
      variant="tonal"
      class="mt-6"
      :text="erro"
    />

    <div class="mt-8 text-center">
      <v-btn
        to="/funcionario"
        variant="tonal"
        class="text-none"
        prepend-icon="mdi-account-tie"
      >
        Ver lista de funcionários
      </v-btn>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getIndicadores, extrairMensagemErro } from "@/services/api";
import type { Indicadores } from "@/types/Funcionario";

const indicadores = ref<Indicadores>({
  total: 0,
  emAnalise: 0,
  aprovados: 0,
  reprovados: 0,
  contratados: 0,
});
const loading = ref(false);
const erro = ref("");

const cards = [
  { key: "emAnalise", label: "Em análise", icon: "mdi-clock-outline", cor: "orange-darken-2" },
  { key: "aprovados", label: "Aprovados", icon: "mdi-check-circle-outline", cor: "primary" },
  { key: "reprovados", label: "Reprovados", icon: "mdi-close-circle-outline", cor: "red-darken-2" },
  { key: "contratados", label: "Contratados", icon: "mdi-briefcase-check-outline", cor: "blue-darken-2" },
] as const;

const proporcao = (valor: number) =>
  indicadores.value.total ? (valor / indicadores.value.total) * 100 : 0;

const percentual = (valor: number) => `${proporcao(valor).toFixed(0)}%`;

const carregar = async () => {
  loading.value = true;
  erro.value = "";
  try {
    const { data } = await getIndicadores();
    indicadores.value = data;
  } catch (error) {
    erro.value = extrairMensagemErro(error, "Erro ao carregar os indicadores.");
  } finally {
    loading.value = false;
  }
};

onMounted(carregar);
</script>
