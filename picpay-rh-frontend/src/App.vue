<template>
  <v-app class="app-shell">
    <v-app-bar flat color="surface" border="b">
      <v-app-bar-title>
        <RouterLink to="/" class="brand">PicPay <span>RH</span></RouterLink>
      </v-app-bar-title>

      <template #append>
        <v-btn
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          :prepend-icon="link.icon"
          variant="text"
          class="text-none"
        >
          {{ link.label }}
        </v-btn>
      </template>
    </v-app-bar>

    <v-main>
      <RouterView />
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { RouterView, RouterLink } from "vue-router";

const links = [
  { to: "/funcionario", label: "Funcionários", icon: "mdi-account-tie" },
  { to: "/indicadores", label: "Indicadores", icon: "mdi-chart-donut" },
];
</script>

<style>
:root {
  --picpay: #21c25e;
  --picpay-dark: #148c42;
}

body {
  margin: 0;
  font-family:
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;
}

.brand {
  color: var(--picpay);
  font-weight: 800;
  letter-spacing: -0.5px;
  text-decoration: none;
}
.brand span {
  color: rgb(var(--v-theme-on-surface));
}
</style>
