package com.trabalhorodolfo.picpayrh.controller;

import com.trabalhorodolfo.picpayrh.entity.Funcionario;
import com.trabalhorodolfo.picpayrh.entity.Status;
import com.trabalhorodolfo.picpayrh.service.FuncionarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FuncionarioController.class)
class FuncionarioControllerTest {

    private static final String CORPO = """
            {"nome": "Ana", "email": "ana@email.com", "cargo": "Dev", "status": "APROVADO"}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FuncionarioService service;

    private Funcionario ana() {
        return new Funcionario(1L, "Ana", "ana@email.com", null, "Dev", null, null, null, Status.APROVADO);
    }

    @Test
    void cadastrarRetorna201ComOFuncionarioCriado() throws Exception {
        when(service.cadastrarFuncionario(any())).thenReturn(ana());

        mvc.perform(post("/funcionarios").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void cadastrarRetorna400QuandoAValidacaoFalha() throws Exception {
        when(service.cadastrarFuncionario(any()))
                .thenThrow(new IllegalArgumentException("O campo 'nome' é obrigatório."));

        mvc.perform(post("/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O campo 'nome' é obrigatório."));
    }

    @Test
    void corpoMalFormatadoRetorna400() throws Exception {
        mvc.perform(post("/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{nao é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O corpo da requisição está ausente ou mal formatado."));
    }

    @Test
    void listarRepassaOsFiltrosParaOService() throws Exception {
        when(service.buscarFuncionarios("ana", "dev", Status.APROVADO)).thenReturn(List.of(ana()));

        mvc.perform(get("/funcionarios")
                        .param("nome", "ana")
                        .param("cargo", "dev")
                        .param("status", "APROVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void statusInvalidoRetorna400ComOsValoresAceitos() throws Exception {
        mvc.perform(get("/funcionarios").param("status", "INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("'INEXISTENTE'")))
                .andExpect(jsonPath("$.erro").value(containsString("EM_ANALISE")));
    }

    @Test
    void idNaoNumericoRetorna400() throws Exception {
        mvc.perform(get("/funcionarios/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("'abc'")));
    }

    @Test
    void indicadoresNaoSaoConfundidosComUmId() throws Exception {
        when(service.gerarIndicadores()).thenReturn(Map.of("total", 5L));

        mvc.perform(get("/funcionarios/indicadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5));
    }

    @Test
    void buscarPorIdRetorna200QuandoExiste() throws Exception {
        when(service.listarUmFuncionarios(1L)).thenReturn(Optional.of(ana()));

        mvc.perform(get("/funcionarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"));
    }

    @Test
    void buscarPorIdRetorna404QuandoNaoExiste() throws Exception {
        when(service.listarUmFuncionarios(42L)).thenReturn(Optional.empty());

        mvc.perform(get("/funcionarios/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Funcionário com id 42 não foi encontrado."));
    }

    @Test
    void atualizarRetorna200QuandoExiste() throws Exception {
        when(service.atualizarFuncionario(eq(1L), any())).thenReturn(Optional.of(ana()));

        mvc.perform(put("/funcionarios/1").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void atualizarRetorna400QuandoAValidacaoFalha() throws Exception {
        when(service.atualizarFuncionario(eq(1L), any()))
                .thenThrow(new IllegalArgumentException("O campo 'cargo' é obrigatório."));

        mvc.perform(put("/funcionarios/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O campo 'cargo' é obrigatório."));
    }

    @Test
    void atualizarRetorna404QuandoNaoExiste() throws Exception {
        when(service.atualizarFuncionario(eq(42L), any())).thenReturn(Optional.empty());

        mvc.perform(put("/funcionarios/42").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarParcialRetorna200QuandoExiste() throws Exception {
        when(service.atualizarParcialFuncionario(eq(1L), any())).thenReturn(Optional.of(ana()));

        mvc.perform(patch("/funcionarios/1").contentType(MediaType.APPLICATION_JSON).content("{\"cargo\": \"Dev\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo").value("Dev"));
    }

    @Test
    void atualizarParcialRetorna404QuandoNaoExiste() throws Exception {
        when(service.atualizarParcialFuncionario(eq(42L), any())).thenReturn(Optional.empty());

        mvc.perform(patch("/funcionarios/42").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirRetorna200QuandoExiste() throws Exception {
        when(service.deletarFuncionario(1L)).thenReturn(true);

        mvc.perform(delete("/funcionarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Funcionário 1 excluído com sucesso."));
    }

    @Test
    void excluirRetorna404QuandoNaoExiste() throws Exception {
        when(service.deletarFuncionario(42L)).thenReturn(false);

        mvc.perform(delete("/funcionarios/42"))
                .andExpect(status().isNotFound());
    }
}
