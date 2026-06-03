package br.com.oficina.oficina.e2e;

import br.com.oficina.oficina.dto.atendimento.AtendimentoDTO;
import br.com.oficina.oficina.dto.atendimento.CadastrarAtendimentoDTO;
import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.dto.cliente.ClienteListaDTO;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.Veiculo;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E2E — CRUD Funcionarios")
class FuncionarioE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private String accessToken;
    private UUID funcionarioIdA;
    private UUID clienteId;
    private String veiculoPlaca;

    @BeforeAll
    void setUp() {
        CadastrarFuncionarioDTO funcA = new CadastrarFuncionarioDTO();
        funcA.setNome("Func Principal E2E");
        funcA.setCpfCNPJ("33344455508");
        funcA.setCargo("ADMIN");
        funcA.setTelefone("11933333333");
        funcA.setEmail("func.principal@oficina.com");
        funcA.setUsuario("func.principal");
        funcA.setSenha("senha123");

        ResponseEntity<FuncionarioDTO> resp = rest.postForEntity("/funcionarios", funcA, FuncionarioDTO.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Falha ao criar funcionario: " + resp.getStatusCode());
        }
        funcionarioIdA = resp.getBody().getId();

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsuario("func.principal");
        loginReq.setSenha("senha123");
        ResponseEntity<AuthResponse> loginResp = rest.postForEntity("/auth/login", loginReq, AuthResponse.class);
        accessToken = loginResp.getBody().getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        CadastrarClienteDTO clienteDTO = new CadastrarClienteDTO();
        clienteDTO.setNomeCompleto("Cliente Func E2E");
        clienteDTO.setCpfCNPJ("11222333000181");
        clienteDTO.setTelefone("11911111111");
        clienteDTO.setEmail("cliente.func.e2e@email.com");
        clienteDTO.setCep("01001000");
        clienteDTO.setNumero("50");

        ResponseEntity<String> clienteResp = rest.exchange(
                "/clientes", HttpMethod.POST, new HttpEntity<>(clienteDTO, headers), String.class);
        if (clienteResp.getStatusCode() != HttpStatus.CREATED) {
            throw new IllegalStateException("Falha ao criar cliente: " + clienteResp.getStatusCode());
        }

        ResponseEntity<List<ClienteListaDTO>> listResp = rest.exchange(
                "/clientes", HttpMethod.GET, new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ClienteListaDTO>>() {});
        clienteId = listResp.getBody().stream()
                .filter(c -> c.getCpfCNPJ().equals("11222333000181"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cliente nao encontrado"))
                .getId();

        CadastrarVeiculoDTO veiculoDTO = new CadastrarVeiculoDTO();
        veiculoDTO.setPlaca("FNC1E99");
        veiculoDTO.setModelo("Uno");
        veiculoDTO.setMarca("Fiat");
        veiculoDTO.setAno(2020);
        veiculoDTO.setClienteId(clienteId);
        ResponseEntity<Veiculo> veicResp = rest.exchange(
                "/veiculos", HttpMethod.POST, new HttpEntity<>(veiculoDTO, headers), Veiculo.class);
        if (veicResp.getStatusCode() != HttpStatus.CREATED) {
            throw new IllegalStateException("Falha ao criar veiculo: " + veicResp.getStatusCode());
        }
        veiculoPlaca = veicResp.getBody().getPlaca();
    }

    private HttpEntity<?> authHeader(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> authHeader() {
        return authHeader(null);
    }

    private CadastrarFuncionarioDTO criarFuncionarioDTO(String cpf, String usuario) {
        CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
        dto.setNome("Funcionario E2E");
        dto.setCpfCNPJ(cpf);
        dto.setCargo("MECANICO");
        dto.setTelefone("11922222222");
        dto.setEmail(usuario + "@oficina.com");
        dto.setUsuario(usuario);
        dto.setSenha("senha123");
        return dto;
    }

    private UUID criarFuncionarioRetornarId(String cpf, String usuario) {
        CadastrarFuncionarioDTO dto = criarFuncionarioDTO(cpf, usuario);
        ResponseEntity<FuncionarioDTO> resp = rest.postForEntity("/funcionarios", dto, FuncionarioDTO.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody().getId();
    }

    @Test
    @DisplayName("POST /funcionarios — deve criar funcionario com sucesso (endpoint publico)")
    void deveCriarFuncionario() {
        CadastrarFuncionarioDTO dto = criarFuncionarioDTO("44455566619", "criado.e2e");

        ResponseEntity<FuncionarioDTO> resp = rest.postForEntity("/funcionarios", dto, FuncionarioDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getId()).isNotNull();
        assertThat(resp.getBody().getNome()).isEqualTo("Funcionario E2E");
        assertThat(resp.getBody().getUsuario()).isEqualTo("criado.e2e");
    }

    @Test
    @DisplayName("POST /funcionarios — deve retornar 400 para dados invalidos")
    void deveRetornar400ParaDadosInvalidos() {
        CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();

        ResponseEntity<String> resp = rest.postForEntity("/funcionarios", dto, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /funcionarios/me — deve retornar 200 com dados do funcionario logado")
    void deveRetornarFuncionarioLogado() {
        ResponseEntity<FuncionarioDTO> resp = rest.exchange(
                "/funcionarios/me", HttpMethod.GET, authHeader(), FuncionarioDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getId()).isEqualTo(funcionarioIdA);
        assertThat(resp.getBody().getUsuario()).isEqualTo("func.principal");
    }

    @Test
    @DisplayName("GET /funcionarios — deve retornar 200 com lista de funcionarios")
    void deveListarTodos() {
        ResponseEntity<FuncionarioDTO[]> resp = rest.exchange(
                "/funcionarios", HttpMethod.GET, authHeader(), FuncionarioDTO[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("GET /funcionarios/{id} — deve retornar 200 com dados do funcionario")
    void deveBuscarPorId() {
        ResponseEntity<FuncionarioDTO> resp = rest.exchange(
                "/funcionarios/" + funcionarioIdA, HttpMethod.GET, authHeader(), FuncionarioDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getId()).isEqualTo(funcionarioIdA);
    }

    @Test
    @DisplayName("GET /funcionarios/{id} — deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/funcionarios/" + UUID.randomUUID(), HttpMethod.GET, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /funcionarios/{id} — deve retornar 200 ao atualizar funcionario")
    void deveAtualizarFuncionario() {
        UUID funcBId = criarFuncionarioRetornarId("55566677720", "atualizado.e2e");

        CadastrarFuncionarioDTO upd = criarFuncionarioDTO("55566677720", "atualizado.e2e");
        upd.setNome("Atualizado E2E");

        ResponseEntity<String> resp = rest.exchange(
                "/funcionarios/" + funcBId, HttpMethod.PUT, authHeader(upd), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("sucesso");
    }

    @Test
    @DisplayName("PUT /funcionarios/{id} — deve retornar 404 ao atualizar ID inexistente")
    void deveRetornar404AoAtualizarIdInexistente() {
        CadastrarFuncionarioDTO dto = criarFuncionarioDTO("77788899941", "inexistente.e2e");

        ResponseEntity<String> resp = rest.exchange(
                "/funcionarios/" + UUID.randomUUID(), HttpMethod.PUT, authHeader(dto), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /funcionarios/{id} — deve retornar 409 ao deletar funcionario com atendimentos")
    void deveRetornar409AoDeletarFuncionarioComAtendimentos() {
        UUID funcId = criarFuncionarioRetornarId("12345678909", "com.atend.e2e");

        CadastrarAtendimentoDTO atdDTO = new CadastrarAtendimentoDTO();
        atdDTO.setDescricaoServico("Atendimento para teste exclusão funcionario");
        atdDTO.setClienteId(clienteId);
        atdDTO.setVeiculoPlaca(veiculoPlaca);
        atdDTO.setFuncionarioId(funcId);

        ResponseEntity<AtendimentoDTO> atdResp = rest.exchange(
                "/atendimentos/cadastrar", HttpMethod.POST, authHeader(atdDTO), AtendimentoDTO.class);
        assertThat(atdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID atendimentoId = atdResp.getBody().getId();

        ResponseEntity<String> deleteResp = rest.exchange(
                "/funcionarios/" + funcId, HttpMethod.DELETE, authHeader(), String.class);

        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        rest.exchange("/atendimentos/delete/" + atendimentoId, HttpMethod.DELETE, authHeader(), String.class);

        ResponseEntity<Void> deleteOk = rest.exchange(
                "/funcionarios/" + funcId, HttpMethod.DELETE, authHeader(), Void.class);
        assertThat(deleteOk.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("DELETE /funcionarios/{id} — deve retornar 204 ao deletar")
    void deveDeletarFuncionario() {
        UUID funcCId = criarFuncionarioRetornarId("66677788830", "deletado.e2e");

        ResponseEntity<Void> resp = rest.exchange(
                "/funcionarios/" + funcCId, HttpMethod.DELETE, authHeader(), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResp = rest.exchange(
                "/funcionarios/" + funcCId, HttpMethod.GET, authHeader(), String.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /funcionarios — deve retornar 409 para usuario duplicado")
    void deveRetornar409ParaUsuarioDuplicado() {
        CadastrarFuncionarioDTO dto = criarFuncionarioDTO("88899911193", "func.principal");

        ResponseEntity<String> resp = rest.postForEntity("/funcionarios", dto, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("GET /funcionarios/me — deve retornar 403 sem token")
    void deveRetornar403SemToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = rest.exchange(
                "/funcionarios/me", HttpMethod.GET,
                new HttpEntity<>(null, headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
