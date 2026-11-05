import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Home.vue"),
  },
  {
    path: "/funcionario",
    name: "funcionario-list",
    component: () => import("@/views/Funcionario/FuncionarioList.vue"),
  },
  {
    path: "/funcionario/:id",
    name: "funcionario-edit",
    component: () => import("@/views/Funcionario/FuncionarioEdit.vue"),
    props: true,
  },
  {
    path: "/indicadores",
    name: "indicadores",
    component: () => import("@/views/Funcionario/FuncionarioIndicadores.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...routes,
    {
      path: "/:pathMatch(.*)*",
      name: "NotFound",
      component: () => import("@/views/404.vue"),
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
});

export default router;
