<template>
  <v-container class="py-8">
    <div class="d-flex align-center flex-wrap ga-4 mb-6">
      <div>
        <h1 class="text-h5 font-weight-bold">Funcionários</h1>
        <p class="text-body-2 text-medium-emphasis mb-0">
          Candidatos do processo de contratação
        </p>
      </div>
      <v-spacer />
      <v-btn
        color="primary"
        prepend-icon="mdi-plus"
        class="text-none"
        @click="createNew"
      >
        Novo Funcionário
      </v-btn>
    </div>

    <v-card variant="outlined" class="mb-6">
      <v-card-text>
        <v-row dense>
          <v-col cols="12" md="4">
            <v-text-field
              v-model="filters.nome"
              label="Nome"
              prepend-inner-icon="mdi-magnify"
              density="comfortable"
              variant="outlined"
              clearable
              hide-details
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field
              v-model="filters.cargo"
              label="Cargo"
              prepend-inner-icon="mdi-briefcase-search-outline"
              density="comfortable"
              variant="outlined"
              clearable
              hide-details
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-select
              v-model="filters.status"
              :items="statusItems"
              label="Status"
              density="comfortable"
              variant="outlined"
              clearable
              hide-details
            />
          </v-col>
        </v-row>

        <div class="d-flex align-center flex-wrap ga-3 mt-4">
          <v-btn
            variant="text"
            size="small"
            class="text-none"
            prepend-icon="mdi-filter-off-outline"
            :disabled="!temFiltro"
            @click="limparFiltros"
          >
            Limpar filtros
          </v-btn>
          <v-spacer />
          <span class="text-caption text-medium-emphasis">
            Última requisição: <code>{{ ultimaRequisicao }}</code>
          </span>
        </div>
      </v-card-text>
    </v-card>

    <v-card variant="tonal" class="mb-6">
      <v-card-text class="d-flex align-center flex-wrap ga-3">
        <v-icon icon="mdi-identifier" />
        <span class="text-body-2">Consultar por ID:</span>
        <v-text-field
          v-model="idConsulta"
          type="number"
          min="1"
          density="compact"
          variant="outlined"
          hide-details
          style="max-width: 140px"
          @keyup.enter="consultarPorId"
        />
        <v-btn
          variant="tonal"
          class="text-none"
          :loading="consultandoId"
          @click="consultarPorId"
        >
          Consultar
        </v-btn>
      </v-card-text>
    </v-card>

    <v-card variant="outlined">
      <v-data-table
        :headers="headers"
        :items="funcionarios"
        :loading="loading"
        item-value="id"
        no-data-text="Nenhum registro encontrado."
        loading-text="Carregando..."
        density="comfortable"
      >
        <template #[`item.salario`]="{ item }">
          {{ formatCurrency(item.salario) }}
        </template>

        <template #[`item.status`]="{ item }">
          <v-chip :color="STATUS_COLORS[item.status]" size="small" label>
            {{ STATUS_LABELS[item.status] ?? item.status }}
          </v-chip>
        </template>

        <template #[`item.actions`]="{ item }">
          <div class="text-no-wrap">
            <v-btn
              icon="mdi-eye-outline"
              variant="text"
              size="small"
              title="Ver detalhes"
              @click="verDetalhes(item.id)"
            />
            <v-btn
              icon="mdi-pencil"
              variant="text"
              size="small"
              title="Editar"
              @click="edit(item.id)"
            />
            <v-btn
              icon="mdi-delete-outline"
              variant="text"
              size="small"
              color="error"
              title="Excluir"
              @click="removeClick(item.id, item.nome)"
            />
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="showDetalhes" max-width="520">
      <v-card v-if="detalhe">
        <v-card-title class="d-flex align-center ga-2">
          <v-icon icon="mdi-account-details-outline" color="primary" />
          {{ detalhe.nome }}
        </v-card-title>
        <v-card-subtitle class="pb-2">
          <code>GET /funcionarios/{{ detalhe.id }}</code>
        </v-card-subtitle>
        <v-card-text>
          <v-table density="compact">
            <tbody>
              <tr v-for="campo in detalheCampos" :key="campo.label">
                <th class="text-left text-medium-emphasis">{{ campo.label }}</th>
                <td>{{ campo.valor }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn class="text-none" @click="showDetalhes = false">Fechar</v-btn>
          <v-btn
            color="primary"
            variant="flat"
            class="text-none"
            @click="edit(detalhe.id)"
          >
            Editar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showDeletePopup" max-width="440">
      <v-card>
        <v-card-title>Excluir funcionário</v-card-title>
        <v-card-text>
          Tem certeza que deseja excluir
          <strong>{{ funcionarioDeleteName }}</strong
          >? Esta ação remove o registro da ArrayList do back-end.
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn class="text-none" @click="showDeletePopup = false">
            Cancelar
          </v-btn>
          <v-btn
            color="error"
            variant="flat"
            class="text-none"
            :loading="excluindo"
            @click="remove"
          >
            Excluir
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import {
  getFuncionarios,
  getFuncionarioById,
  deleteFuncionario,
  extrairMensagemErro,
  type FuncionarioFiltros,
} from "@/services/api";
import type Funcionario from "@/types/Funcionario";
import {
  STATUS_COLORS,
  STATUS_LABELS,
  STATUS_VALUES,
} from "@/types/Funcionario";

const router = useRouter();
const funcionarios = ref<Funcionario[]>([]);
const loading = ref(false);

const formatCurrency = (value: number | null) => {
  if (value === null || value === undefined || value === 0) return "";
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(Number(value));
};
const ultimaRequisicao = ref("GET /funcionarios");

const headers = [
  { title: "Ações", key: "actions", align: "center", sortable: false },
  { title: "ID", key: "id", align: "start" },
  { title: "Nome", key: "nome", align: "start" },
  { title: "E-mail", key: "email", align: "start" },
  { title: "Telefone", key: "telefone", align: "start" },
  { title: "Cargo", key: "cargo", align: "start" },
  { title: "Departamento", key: "departamento", align: "start" },
  { title: "Salário", key: "salario", align: "start" },
  { title: "Cidade", key: "cidade", align: "start" },
  { title: "Status", key: "status", align: "start" },
] as const;

const statusItems = STATUS_VALUES.map((value) => ({
  value,
  title: STATUS_LABELS[value],
}));

const filters = reactive<FuncionarioFiltros>({
  nome: "",
  cargo: "",
  status: "",
});

const temFiltro = computed(() =>
  Boolean(filters.nome || filters.cargo || filters.status),
);

const limparFiltros = () => {
  filters.nome = "";
  filters.cargo = "";
  filters.status = "";
};

const loadFuncionarios = async () => {
  loading.value = true;
  try {
    const { data } = await getFuncionarios(filters);
    funcionarios.value = data;
    const params = new URLSearchParams();
    if (filters.nome) params.set("nome", filters.nome);
    if (filters.cargo) params.set("cargo", filters.cargo);
    if (filters.status) params.set("status", filters.status);
    const query = params.toString();
    ultimaRequisicao.value = `GET /funcionarios${query ? "?" + query : ""}`;
  } catch (error) {
    funcionarios.value = [];
    toast.error(extrairMensagemErro(error, "Erro ao carregar os funcionários."));
  } finally {
    loading.value = false;
  }
};

let debounce: ReturnType<typeof setTimeout> | undefined;
watch(filters, () => {
  clearTimeout(debounce);
  debounce = setTimeout(loadFuncionarios, 350);
});

onMounted(loadFuncionarios);
onUnmounted(() => clearTimeout(debounce));

const createNew = () => {
  router.push({ name: "funcionario-edit", params: { id: 0 } });
};

const edit = (id: number) => {
  router.push({ name: "funcionario-edit", params: { id } });
};

const idConsulta = ref<string>("");
const consultandoId = ref(false);
const showDetalhes = ref(false);
const detalhe = ref<Funcionario | null>(null);

const buscarDetalhe = async (id: number) => {
  const { data } = await getFuncionarioById(id);
  detalhe.value = data;
  showDetalhes.value = true;
};

const consultarPorId = async () => {
  const id = Number(idConsulta.value);
  if (!id) {
    toast.warning("Informe um ID válido.");
    return;
  }
  consultandoId.value = true;
  try {
    await buscarDetalhe(id);
  } catch (error) {
    toast.error(
      extrairMensagemErro(error, `Funcionário com id ${id} não encontrado.`),
    );
  } finally {
    consultandoId.value = false;
  }
};

const verDetalhes = async (id: number) => {
  try {
    await buscarDetalhe(id);
  } catch (error) {
    toast.error(extrairMensagemErro(error));
  }
};

const detalheCampos = computed(() => {
  const f = detalhe.value;
  if (!f) return [];
  return [
    { label: "ID", valor: String(f.id) },
    { label: "Nome", valor: f.nome },
    { label: "E-mail", valor: f.email },
    { label: "Telefone", valor: f.telefone || "—" },
    { label: "Cargo", valor: f.cargo },
    { label: "Departamento", valor: f.departamento || "—" },
    { label: "Salário", valor: formatCurrency(f.salario) || "—" },
    { label: "Cidade", valor: f.cidade || "—" },
    { label: "Status", valor: STATUS_LABELS[f.status] ?? f.status },
  ];
});

const showDeletePopup = ref(false);
const excluindo = ref(false);
const funcionarioDeleteId = ref(0);
const funcionarioDeleteName = ref("");

const removeClick = (id: number, name: string) => {
  funcionarioDeleteId.value = id;
  funcionarioDeleteName.value = name;
  showDeletePopup.value = true;
};

const remove = async () => {
  excluindo.value = true;
  try {
    const { data } = await deleteFuncionario(funcionarioDeleteId.value);
    toast.success(data?.mensagem ?? "Funcionário excluído com sucesso.");
    showDeletePopup.value = false;
    await loadFuncionarios();
  } catch (error) {
    toast.error(extrairMensagemErro(error, "Erro ao excluir o funcionário."));
  } finally {
    excluindo.value = false;
  }
};
</script>
