package com.trabalhorodolfo.picpayrh.service;

import com.trabalhorodolfo.picpayrh.entity.Funcionario;
import com.trabalhorodolfo.picpayrh.entity.Status;
import com.trabalhorodolfo.picpayrh.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FuncionarioServiceTest {

    private FuncionarioRepository repository;
    private FuncionarioService service;

    // Sem o Spring, o @PostConstruct não roda: cada teste começa com o repositório vazio.
    @BeforeEach
    void setUp() {
        repository = new FuncionarioRepository();
        service = new FuncionarioService(repository);
    }

    private Funcionario funcionario(String nome, String cargo, Status status) {
        return new Funcionario(null, nome, nome.toLowerCase() + "@email.com", "(11) 90000-0000",
                cargo, "Tecnologia", 5000.0, "São Paulo", status);
    }

    private Funcionario salvar(String nome, String cargo, Status status) {
        return repository.salvar(funcionario(nome, cargo, status));
    }

    @Test
    void cadastrarGeraIdEPersiste() {
        Funcionario criado = service.cadastrarFuncionario(funcionario("Ana", "Dev", Status.APROVADO));

        assertThat(criado.getId()).isNotNull();
        assertThat(service.listarUmFuncionarios(criado.getId())).contains(criado);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULO", value = {
            "nome,  NULO",
            "nome,  '   '",
            "email, NULO",
            "email, '   '",
            "cargo, NULO",
            "cargo, '   '",
    })
    void cadastrarRejeitaCampoObrigatorioAusente(String campo, String valor) {
        Funcionario invalido = funcionario("Ana", "Dev", Status.APROVADO);
        switch (campo) {
            case "nome" -> invalido.setNome(valor);
            case "email" -> invalido.setEmail(valor);
            default -> invalido.setCargo(valor);
        }

        assertThatThrownBy(() -> service.cadastrarFuncionario(invalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O campo '" + campo + "' é obrigatório.");
        assertThat(service.listarFuncionarios()).isEmpty();
    }

    @Test
    void atualizarSubstituiTodosOsCamposEMantemOId() {
        Long id = salvar("Ana", "Dev", Status.EM_ANALISE).getId();
        Funcionario dados = new Funcionario(999L, "Bia", "bia@email.com", null,
                "Analista", "Produto", 7000.0, "Recife", Status.CONTRATADO);

        Funcionario atualizado = service.atualizarFuncionario(id, dados).orElseThrow();

        assertThat(atualizado.getId()).isEqualTo(id);
        assertThat(atualizado.getNome()).isEqualTo("Bia");
        assertThat(atualizado.getEmail()).isEqualTo("bia@email.com");
        assertThat(atualizado.getTelefone()).isNull();
        assertThat(atualizado.getCargo()).isEqualTo("Analista");
        assertThat(atualizado.getDepartamento()).isEqualTo("Produto");
        assertThat(atualizado.getSalario()).isEqualTo(7000.0);
        assertThat(atualizado.getCidade()).isEqualTo("Recife");
        assertThat(atualizado.getStatus()).isEqualTo(Status.CONTRATADO);
    }

    @Test
    void atualizarParcialAlteraSomenteOsCamposInformados() {
        Long id = salvar("Ana", "Dev", Status.EM_ANALISE).getId();
        Funcionario dados = new Funcionario();
        dados.setStatus(Status.APROVADO);
        dados.setSalario(9000.0);

        Funcionario atualizado = service.atualizarParcialFuncionario(id, dados).orElseThrow();

        assertThat(atualizado.getStatus()).isEqualTo(Status.APROVADO);
        assertThat(atualizado.getSalario()).isEqualTo(9000.0);
        assertThat(atualizado.getNome()).isEqualTo("Ana");
        assertThat(atualizado.getCargo()).isEqualTo("Dev");
    }

    @Test
    void atualizacoesDeIdInexistenteRetornamVazio() {
        assertThat(service.atualizarFuncionario(42L, new Funcionario())).isEmpty();
        assertThat(service.atualizarParcialFuncionario(42L, new Funcionario())).isEmpty();
    }

    @Test
    void deletarRemoveEInformaSeEncontrou() {
        Long id = salvar("Ana", "Dev", Status.APROVADO).getId();

        assertThat(service.deletarFuncionario(id)).isTrue();
        assertThat(service.listarUmFuncionarios(id)).isEmpty();
        assertThat(service.deletarFuncionario(id)).isFalse();
    }

    @Test
    void buscarFiltraPorParteDoTextoSemDiferenciarMaiusculas() {
        salvar("Ana Souza", "Desenvolvedora", Status.APROVADO);
        salvar("Bruno Lima", "Analista", Status.REPROVADO);

        assertThat(nomes(service.buscarFuncionarios("SOUZA", null, null))).containsExactly("Ana Souza");
        assertThat(nomes(service.buscarFuncionarios(null, "anal", null))).containsExactly("Bruno Lima");
    }

    @Test
    void buscarFiltraPorStatusECombinaOsFiltros() {
        salvar("Ana Souza", "Dev", Status.APROVADO);
        salvar("Ana Lima", "Dev", Status.REPROVADO);
        salvar("Bruno Lima", "Dev", Status.APROVADO);

        assertThat(nomes(service.buscarFuncionarios(null, null, Status.REPROVADO))).containsExactly("Ana Lima");
        assertThat(nomes(service.buscarFuncionarios("ana", "dev", Status.APROVADO))).containsExactly("Ana Souza");
    }

    @Test
    void buscarIgnoraFiltrosVazios() {
        salvar("Ana", "Dev", Status.APROVADO);
        salvar("Bruno", "Analista", Status.REPROVADO);

        assertThat(service.buscarFuncionarios(null, null, null)).hasSize(2);
        assertThat(service.buscarFuncionarios("  ", "", null)).hasSize(2);
    }

    @Test
    void indicadoresContamOTotalECadaStatus() {
        // Uma quantidade diferente por status, para que uma troca entre eles seja detectada.
        salvar("Ana", "Dev", Status.EM_ANALISE);
        salvar("Bia", "Dev", Status.EM_ANALISE);
        salvar("Caio", "Dev", Status.EM_ANALISE);
        salvar("Duda", "Dev", Status.EM_ANALISE);
        salvar("Enzo", "Dev", Status.APROVADO);
        salvar("Fabi", "Dev", Status.APROVADO);
        salvar("Gabi", "Dev", Status.APROVADO);
        salvar("Hugo", "Dev", Status.REPROVADO);
        salvar("Iara", "Dev", Status.REPROVADO);
        salvar("João", "Dev", Status.CONTRATADO);

        assertThat(service.gerarIndicadores()).isEqualTo(Map.of(
                "total", 10L,
                "emAnalise", 4L,
                "aprovados", 3L,
                "reprovados", 2L,
                "contratados", 1L));
    }

    private List<String> nomes(List<Funcionario> funcionarios) {
        return funcionarios.stream().map(Funcionario::getNome).toList();
    }
}
