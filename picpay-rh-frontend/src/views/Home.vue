<template>
  <v-container class="py-10">
    <div class="text-center mb-10">
      <h1 class="header-label text-h3 mb-2">PicPay RH</h1>
      <p class="text-subtitle-1 text-medium-emphasis">
        Sistema de contratação de funcionários
      </p>
    </div>

    <v-row justify="center" class="mb-8">
      <v-col v-for="card in cards" :key="card.to" cols="12" sm="6" md="4">
        <v-card :to="card.to" variant="outlined" class="h-100 pa-2" hover>
          <v-card-item>
            <v-icon :icon="card.icon" color="primary" size="36" class="mb-3" />
            <v-card-title class="text-wrap">{{ card.title }}</v-card-title>
            <v-card-text class="px-0 text-medium-emphasis">
              {{ card.text }}
            </v-card-text>
          </v-card-item>
        </v-card>
      </v-col>
    </v-row>

    <v-card variant="tonal" max-width="720" class="mx-auto">
      <v-card-title class="text-subtitle-1">
        <v-icon icon="mdi-lan-connect" size="20" class="mr-1" />
        Comunicação com o back-end
      </v-card-title>
      <v-card-text>
        <p class="mb-3">
          Todas as telas conversam com a API Spring Boot por requisições HTTP
          feitas com <strong>axios</strong>:
        </p>
        <pre class="fluxo">Vue → HTTP (axios) → Spring Boot REST API → ArrayList</pre>
        <v-table density="compact" class="mt-4 bg-transparent">
          <tbody>
            <tr v-for="op in operacoes" :key="op.metodo + op.rota">
              <td class="py-1">
                <v-chip :color="op.cor" size="small" label>{{ op.metodo }}</v-chip>
              </td>
              <td class="py-1"><code>{{ op.rota }}</code></td>
              <td class="py-1 text-medium-emphasis">{{ op.descricao }}</td>
            </tr>
          </tbody>
        </v-table>
        <p class="text-caption text-medium-emphasis mt-3 mb-0">
          API configurada em <code>{{ apiUrl }}</code> (variável
          <code>VITE_API_URL</code>).
        </p>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import api from "@/services/api";

const apiUrl = api.defaults.baseURL;

const cards = [
  {
    to: "/funcionario",
    icon: "mdi-account-tie",
    title: "Funcionários",
    text: "Listar, pesquisar, cadastrar, editar e excluir candidatos.",
  },
  {
    to: "/indicadores",
    icon: "mdi-chart-donut",
    title: "Indicadores",
    text: "Totais do processo seletivo por status de contratação.",
  },
];

const operacoes = [
  { metodo: "POST", rota: "/funcionarios", descricao: "Cadastrar", cor: "primary" },
  { metodo: "GET", rota: "/funcionarios", descricao: "Consultar todos / pesquisar", cor: "blue-darken-2" },
  { metodo: "GET", rota: "/funcionarios/{id}", descricao: "Consultar por ID", cor: "blue-darken-2" },
  { metodo: "PUT", rota: "/funcionarios/{id}", descricao: "Atualizar completamente", cor: "orange-darken-2" },
  { metodo: "PATCH", rota: "/funcionarios/{id}", descricao: "Atualizar parcialmente", cor: "purple-darken-2" },
  { metodo: "DELETE", rota: "/funcionarios/{id}", descricao: "Excluir", cor: "red-darken-2" },
];
</script>

<style scoped>
.header-label {
  color: var(--picpay);
  font-weight: 800;
  letter-spacing: -1px;
}
.fluxo {
  background: rgba(33, 194, 94, 0.12);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  overflow-x: auto;
  margin: 0;
}
</style>
