import { createApp, type ObjectDirective } from "vue";
import "vue3-toastify/dist/index.css";
import Vue3Toastify, { type ToastContainerOptions } from "vue3-toastify";
import '@mdi/font/css/materialdesignicons.css'
import "vuetify/styles";
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import App from "./App.vue";
import router from "./router";
const app = createApp(App);

const vuetify = createVuetify({
  components: {
    ...components
  },
  directives,
  theme: {
    defaultTheme: "light",
    themes: {
      light: {
        colors: {
          primary: "#21C25E",
        },
      },
    },
  },
});

app.use(Vue3Toastify, {
  clearOnUrlChange: false,
  autoClose: 4000,
  position: "top-right",
} as ToastContainerOptions);

app.use(router);
app.use(vuetify);
app.mount("#app");
