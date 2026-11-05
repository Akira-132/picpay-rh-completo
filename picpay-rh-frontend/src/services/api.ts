import axios, { AxiosError } from "axios";
import type Funcionario from "@/types/Funcionario";
import type { Indicadores, Status } from "@/types/Funcionario";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

export interface FuncionarioFiltros {
  nome?: string;
  cargo?: string;
  status?: Status | "";
}

export const getFuncionarios = (filtros: FuncionarioFiltros = {}) =>
  api.get<Funcionario[]>("/funcionarios", {
    params: {
      nome: filtros.nome || undefined,
      cargo: filtros.cargo || undefined,
      status: filtros.status || undefined,
    },
  });

export const getFuncionarioById = (id: number) =>
  api.get<Funcionario>(`/funcionarios/${id}`);

export const getIndicadores = () =>
  api.get<Indicadores>("/funcionarios/indicadores");

export const createFuncionario = (funcionario: Funcionario) =>
  api.post<Funcionario>("/funcionarios", funcionario);

export const updateFuncionario = (id: number, funcionario: Funcionario) =>
  api.put<Funcionario>(`/funcionarios/${id}`, funcionario);

export const patchFuncionario = (
  id: number,
  campos: Partial<Funcionario>,
) => api.patch<Funcionario>(`/funcionarios/${id}`, campos);

export const deleteFuncionario = (id: number) =>
  api.delete<{ mensagem: string }>(`/funcionarios/${id}`);

export const extrairMensagemErro = (
  erro: unknown,
  padrao = "Não foi possível concluir a operação.",
): string => {
  if (erro instanceof AxiosError) {
    const corpo = erro.response?.data as { erro?: string } | undefined;
    if (corpo?.erro) return corpo.erro;
    if (!erro.response) {
      return "Não foi possível falar com a API. O Spring Boot está rodando em " +
        api.defaults.baseURL +
        "?";
    }
  }
  return padrao;
};

export default api;
