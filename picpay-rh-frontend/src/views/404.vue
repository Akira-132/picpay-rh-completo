<template>
  <v-container class="py-16 text-center">
    <v-icon icon="mdi-map-marker-question-outline" size="64" color="primary" />
    <h1 class="text-h4 font-weight-bold mt-4 mb-2">404 — Página não encontrada</h1>
    <p class="text-body-1 text-medium-emphasis mb-6">
      O endereço acessado não existe neste sistema.
    </p>
    <div class="d-flex justify-center flex-wrap ga-3">
      <v-btn to="/" color="primary" class="text-none" prepend-icon="mdi-home">
        Voltar para a Home
      </v-btn>
      <v-btn
        to="/funcionario"
        variant="tonal"
        class="text-none"
        prepend-icon="mdi-account-tie"
      >
        Ver funcionários
      </v-btn>
    </div>
  </v-container>
</template>
