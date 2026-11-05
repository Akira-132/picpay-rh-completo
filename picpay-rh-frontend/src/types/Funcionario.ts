export const STATUS_VALUES = [
  "EM_ANALISE",
  "APROVADO",
  "REPROVADO",
  "CONTRATADO",
] as const;

export type Status = (typeof STATUS_VALUES)[number];

export const STATUS_LABELS: Record<Status, string> = {
  EM_ANALISE: "Em análise",
  APROVADO: "Aprovado",
  REPROVADO: "Reprovado",
  CONTRATADO: "Contratado",
};

export const STATUS_COLORS: Record<Status, string> = {
  EM_ANALISE: "orange-darken-2",
  APROVADO: "primary",
  REPROVADO: "red-darken-2",
  CONTRATADO: "blue-darken-2",
};

export default interface Funcionario {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  cargo: string;
  departamento: string;
  salario: number | null;
  cidade: string;
  status: Status;
}

export interface Indicadores {
  total: number;
  emAnalise: number;
  aprovados: number;
  reprovados: number;
  contratados: number;
}

export const novoFuncionario = (): Funcionario => ({
  id: 0,
  nome: "",
  email: "",
  telefone: "",
  cargo: "",
  departamento: "",
  salario: null,
  cidade: "",
  status: "EM_ANALISE",
});
