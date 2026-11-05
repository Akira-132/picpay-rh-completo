<template>
  <v-container class="py-8" style="max-width: 1100px">
    <div class="d-flex align-center flex-wrap ga-4 mb-6">
      <div>
        <h1 class="text-h5 font-weight-bold">
          {{ isNew ? "Novo Funcionário" : "Editar Funcionário" }}
        </h1>
        <p class="text-body-2 text-medium-emphasis mb-0">
          <code>{{ metodoDescricao }}</code>
        </p>
      </div>
      <v-spacer />
      <v-btn
        variant="text"
        class="text-none"
        prepend-icon="mdi-arrow-left"
        @click="onBack"
      >
        Voltar
      </v-btn>
      <v-btn
        color="primary"
        class="text-none"
        prepend-icon="mdi-content-save-outline"
        :loading="salvando"
        @click="onSave"
      >
        Salvar
      </v-btn>
    </div>

    <v-alert
      v-if="!isNew"
      variant="tonal"
      :color="isPartialUpdate ? 'purple-darken-2' : 'orange-darken-2'"
      class="mb-6"
    >
      <div class="d-flex align-center flex-wrap ga-4">
        <v-switch
          v-model="isPartialUpdate"
          color="purple-darken-2"
          label="Usar atualização parcial (PATCH)"
          hide-details
          density="compact"
          class="flex-grow-0"
        />
        <span class="text-body-2">
          {{
            isPartialUpdate
              ? "Somente os campos alterados serão enviados ao back-end."
              : "Todos os campos do formulário serão enviados e sobrescritos."
          }}
        </span>
      </div>
      <div v-if="isPartialUpdate && camposAlterados.length" class="mt-2">
        <span class="text-caption text-medium-emphasis mr-2">
          Campos que serão enviados:
        </span>
        <v-chip
          v-for="campo in camposAlterados"
          :key="campo"
          size="x-small"
          label
          class="mr-1"
        >
          {{ campo }}
        </v-chip>
      </div>
    </v-alert>

    <v-card variant="outlined" :loading="carregando">
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="onSave">
          <v-row dense>
            <v-col cols="12" md="4">
              <v-text-field
                :model-value="isNew ? '' : funcionario.id"
                label="ID"
                placeholder="Gerado automaticamente"
                variant="outlined"
                density="comfortable"
                readonly
                disabled
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="funcionario.nome"
                label="Nome *"
                placeholder="Ex.: João da Silva"
                variant="outlined"
                density="comfortable"
                :rules="[regras.obrigatorio, regras.max200]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="funcionario.email"
                label="E-mail *"
                type="email"
                placeholder="Ex.: joao@email.com"
                variant="outlined"
                density="comfortable"
                :rules="[regras.obrigatorio, regras.email]"
              />
            </v-col>

            <v-col cols="12" md="4">
              <v-text-field
                v-model="funcionario.telefone"
                label="Telefone"
                placeholder="Ex.: (11) 99999-9999"
                variant="outlined"
                density="comfortable"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="funcionario.cargo"
                label="Cargo *"
                placeholder="Ex.: Desenvolvedor Front-end"
                variant="outlined"
                density="comfortable"
                :rules="[regras.obrigatorio]"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="funcionario.departamento"
                label="Departamento"
                placeholder="Ex.: Tecnologia"
                variant="outlined"
                density="comfortable"
              />
            </v-col>

            <v-col cols="12" md="4">
              <v-text-field
                v-model="salarioModel"
                label="Salário"
                type="number"
                step="0.01"
                min="0"
                prefix="R$"
                placeholder="Ex.: 5000.00"
                variant="outlined"
                density="comfortable"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="funcionario.cidade"
                label="Cidade"
                placeholder="Ex.: São Paulo"
                variant="outlined"
                density="comfortable"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-select
                v-model="funcionario.status"
                :items="statusItems"
                label="Status"
                variant="outlined"
                density="comfortable"
              />
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
    </v-card>

    <v-dialog v-model="showOnBackPopup" max-width="440" persistent>
      <v-card>
        <v-card-title>Alterações não salvas</v-card-title>
        <v-card-text>
          Você tem alterações não salvas. Deseja descartá-las e sair?
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn class="text-none" @click="cancelExit">Não, ficar aqui</v-btn>
          <v-btn color="error" variant="flat" class="text-none" @click="confirmExit">
            Sim, descartar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useRoute, useRouter, onBeforeRouteLeave } from "vue-router";
import { toast } from "vue3-toastify";
import {
  createFuncionario,
  getFuncionarioById,
  updateFuncionario,
  patchFuncionario,
  extrairMensagemErro,
} from "@/services/api";
import type Funcionario from "@/types/Funcionario";
import {
  STATUS_LABELS,
  STATUS_VALUES,
  novoFuncionario,
} from "@/types/Funcionario";

const router = useRouter();
const route = useRoute();

const funcionario = reactive<Funcionario>(novoFuncionario());
const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null);
const carregando = ref(false);
const salvando = ref(false);
const originalSnapshot = ref("");
const isPartialUpdate = ref(false);

const isNew = computed(() => route.params.id?.toString() === "0");

const statusItems = STATUS_VALUES.map((value) => ({
  value,
  title: STATUS_LABELS[value],
}));

const regras = {
  obrigatorio: (v: unknown) =>
    (v !== null && v !== undefined && String(v).trim() !== "") ||
    "Campo obrigatório.",
  max200: (v: unknown) =>
    String(v ?? "").length <= 200 || "Máximo de 200 caracteres.",
  email: (v: unknown) =>
    /^\S+@\S+\.\S+$/.test(String(v ?? "")) || "Informe um e-mail válido.",
};

const salarioModel = computed({
  get: () => (funcionario.salario === null ? "" : String(funcionario.salario)),
  set: (v: string) => {
    funcionario.salario = v === "" ? null : Number(v);
  },
});

const payload = (): Funcionario => ({ ...funcionario });
const snapshot = () => JSON.stringify(payload());
const isDirty = computed(() => snapshot() !== originalSnapshot.value);

originalSnapshot.value = snapshot();

const metodoDescricao = computed(() => {
  if (isNew.value) return "POST /funcionarios";
  const metodo = isPartialUpdate.value ? "PATCH" : "PUT";
  return `${metodo} /funcionarios/${funcionario.id}`;
});

const diffParcial = (): Partial<Funcionario> => {
  const original = JSON.parse(originalSnapshot.value || "{}") as Funcionario;
  const atual = payload();
  const alterados: Partial<Funcionario> = {};
  for (const key of Object.keys(atual) as (keyof Funcionario)[]) {
    if (key === "id") continue;
    if (atual[key] !== original[key]) {
      alterados[key] = atual[key] as never;
    }
  }
  return alterados;
};

const camposAlterados = computed(() => {
  void isDirty.value;
  return Object.keys(diffParcial());
});

const loadFuncionario = async () => {
  if (isNew.value) {
    originalSnapshot.value = snapshot();
    return;
  }

  carregando.value = true;
  try {
    const { data } = await getFuncionarioById(Number(route.params.id));
    Object.assign(funcionario, data);
    originalSnapshot.value = snapshot();
  } catch (error) {
    toast.error(extrairMensagemErro(error, "Funcionário não encontrado."));
    originalSnapshot.value = snapshot();
    router.replace({ name: "funcionario-list" });
  } finally {
    carregando.value = false;
  }
};

const onSave = async () => {
  const validacao = await formRef.value?.validate();
  if (validacao && !validacao.valid) {
    toast.warning("Preencha corretamente os campos obrigatórios.");
    return;
  }

  salvando.value = true;
  try {
    if (isNew.value) {
      const { data } = await createFuncionario(payload());
      toast.success(`Funcionário cadastrado com o id ${data.id}.`);
    } else if (isPartialUpdate.value) {
      const alterados = diffParcial();
      if (Object.keys(alterados).length === 0) {
        toast.info("Nenhuma alteração detectada para enviar no PATCH.");
        return;
      }
      await patchFuncionario(funcionario.id, alterados);
      toast.success(
        `Atualização parcial enviada (${Object.keys(alterados).join(", ")}).`,
      );
    } else {
      await updateFuncionario(funcionario.id, payload());
      toast.success("Funcionário atualizado com sucesso.");
    }

    originalSnapshot.value = snapshot();
    router.push({ name: "funcionario-list" });
  } catch (error) {
    toast.error(extrairMensagemErro(error, "Erro ao salvar o registro."));
  } finally {
    salvando.value = false;
  }
};

const showOnBackPopup = ref(false);
const confirmDiscard = ref(false);
const rotaPendente = ref<string | null>(null);

const onBack = () => {
  router.push({ name: "funcionario-list" });
};

onBeforeRouteLeave((to) => {
  if (!isDirty.value || confirmDiscard.value) return true;
  rotaPendente.value = to.fullPath;
  showOnBackPopup.value = true;
  return false;
});

const cancelExit = () => {
  showOnBackPopup.value = false;
  rotaPendente.value = null;
};

const confirmExit = () => {
  showOnBackPopup.value = false;
  confirmDiscard.value = true;
  const destino = rotaPendente.value;
  rotaPendente.value = null;
  router.push(destino ?? { name: "funcionario-list" });
};

watch(
  () => route.params.id,
  () => {
    if (route.name !== "funcionario-edit") return;
    Object.assign(funcionario, novoFuncionario());
    originalSnapshot.value = snapshot();
    loadFuncionario();
  },
);

onMounted(loadFuncionario);
</script>
