package br.com.oficina.oficina.e2e;

import br.com.oficina.oficina.config.TestConfig;
import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.dto.cliente.ClienteListaDTO;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.model.Cliente;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E2E — CRUD Clientes")
class ClienteE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private String accessToken;

    @BeforeAll
    void setUp() {
        CadastrarFuncionarioDTO funcDTO = new CadastrarFuncionarioDTO();
        funcDTO.setNome("Funcionario E2E");
        funcDTO.setCpfCNPJ("87654321007");
        funcDTO.setCargo("ADMIN");
        funcDTO.setTelefone("11988888888");
        funcDTO.setEmail("func.e2e@oficina.com");
        funcDTO.setUsuario("func.e2e");
        funcDTO.setSenha("senha123");

        ResponseEntity<Object> createResp = rest.postForEntity("/funcionarios", funcDTO, Object.class);
        if (createResp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Falha ao criar funcionário: " + createResp.getStatusCode());
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsuario("func.e2e");
        loginReq.setSenha("senha123");

        ResponseEntity<AuthResponse> loginResp = rest.postForEntity("/auth/login", loginReq, AuthResponse.class);
        accessToken = loginResp.getBody().getAccessToken();
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

    private String createAndReturnId(CadastrarClienteDTO dto) {
        rest.exchange("/clientes", HttpMethod.POST, authHeader(dto), String.class);
        ResponseEntity<List<ClienteListaDTO>> listResp = rest.exchange(
                "/clientes", HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<ClienteListaDTO>>() {});
        return listResp.getBody().stream()
                .filter(c -> c.getCpfCNPJ().equals(dto.getCpfCNPJ()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cliente não encontrado após criação"))
                .getId()
                .toString();
    }

    // =========================================================================
    // POST /clientes
    // =========================================================================

    @Test
    @DisplayName("POST /clientes — deve criar cliente com sucesso e retornar 201")
    void deveCriarCliente() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Maria Souza");
        dto.setCpfCNPJ("52998224725");
        dto.setTelefone("11987654321");
        dto.setEmail("maria.criar@email.com");
        dto.setCep("01001000");
        dto.setNumero("100");
        dto.setComplemento("Apto 1");

        ResponseEntity<String> resp = rest.exchange(
                "/clientes", HttpMethod.POST, authHeader(dto), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).contains("Cliente cadastrado com sucesso");
    }

    @Test
    @DisplayName("POST /clientes — deve retornar 409 para CPF já cadastrado")
    void deveRetornar409ParaCpfDuplicado() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Primeiro");
        dto.setCpfCNPJ("35814796219");
        dto.setTelefone("11900000001");
        dto.setEmail("primeiro@email.com");
        dto.setCep("01001000");
        dto.setNumero("10");
        rest.exchange("/clientes", HttpMethod.POST, authHeader(dto), String.class);

        CadastrarClienteDTO dup = new CadastrarClienteDTO();
        dup.setNomeCompleto("Duplicado");
        dup.setCpfCNPJ("35814796219");
        dup.setTelefone("11900000002");
        dup.setEmail("duplicado@email.com");
        dup.setCep("01001000");
        dup.setNumero("20");

        ResponseEntity<String> resp = rest.exchange(
                "/clientes", HttpMethod.POST, authHeader(dup), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("POST /clientes — deve retornar 400 para dados inválidos")
    void deveRetornar400ParaDadosInvalidos() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("");
        dto.setCpfCNPJ("52998224725");
        dto.setTelefone("11987654321");
        dto.setEmail("invalido@email.com");
        dto.setCep("01001000");
        dto.setNumero("100");

        ResponseEntity<String> resp = rest.exchange(
                "/clientes", HttpMethod.POST, authHeader(dto), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /clientes — deve retornar 403 sem token")
    void deveRetornar403SemToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Sem Token");
        dto.setCpfCNPJ("11144477735");
        dto.setTelefone("11900000000");
        dto.setEmail("semtoken@email.com");
        dto.setCep("01001000");
        dto.setNumero("1");

        ResponseEntity<String> resp = rest.exchange(
                "/clientes", HttpMethod.POST,
                new HttpEntity<>(dto, headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // =========================================================================
    // GET /clientes
    // =========================================================================

    @Test
    @DisplayName("GET /clientes — deve retornar 200 com lista de clientes")
    void deveListarClientes() {
        CadastrarClienteDTO c1 = new CadastrarClienteDTO();
        c1.setNomeCompleto("Lista A");
        c1.setCpfCNPJ("14567903072");
        c1.setTelefone("11900000003");
        c1.setEmail("listaa@email.com");
        c1.setCep("01001000");
        c1.setNumero("2");
        rest.exchange("/clientes", HttpMethod.POST, authHeader(c1), String.class);

        CadastrarClienteDTO c2 = new CadastrarClienteDTO();
        c2.setNomeCompleto("Lista B");
        c2.setCpfCNPJ("07562078009");
        c2.setTelefone("11900000004");
        c2.setEmail("listab@email.com");
        c2.setCep("01001000");
        c2.setNumero("3");
        rest.exchange("/clientes", HttpMethod.POST, authHeader(c2), String.class);

        ResponseEntity<List<ClienteListaDTO>> resp = rest.exchange(
                "/clientes", HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<ClienteListaDTO>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).extracting("nomeCompleto").contains("Lista A", "Lista B");
    }

    @Test
    @DisplayName("GET /clientes — deve retornar 403 sem token")
    void deveRetornar403SemTokenAolistar() {
        ResponseEntity<String> resp = rest.exchange(
                "/clientes", HttpMethod.GET, null, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // =========================================================================
    // GET /clientes/{id}
    // =========================================================================

    @Test
    @DisplayName("GET /clientes/{id} — deve retornar 200 com dados do cliente")
    void deveBuscarPorId() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Busca ID");
        dto.setCpfCNPJ("11122233396");
        dto.setTelefone("11900000005");
        dto.setEmail("buscavid@email.com");
        dto.setCep("01001000");
        dto.setNumero("5");
        String id = createAndReturnId(dto);

        ResponseEntity<Cliente> resp = rest.exchange(
                "/clientes/" + id, HttpMethod.GET, authHeader(), Cliente.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNomeCompleto()).isEqualTo("Busca ID");
        assertThat(resp.getBody().getCpfCNPJ()).isEqualTo("11122233396");
    }

    @Test
    @DisplayName("GET /clientes/{id} — deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/clientes/" + UUID.randomUUID(), HttpMethod.GET, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================================
    // GET /clientes/cpfCNPJ/{cpfCNPJ}
    // =========================================================================

    @Test
    @DisplayName("GET /clientes/cpfCNPJ/{cpfCNPJ} — deve retornar 200 com dados do cliente")
    void deveBuscarPorCpf() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Busca CPF");
        dto.setCpfCNPJ("12345678909");
        dto.setTelefone("11900000006");
        dto.setEmail("buscacpf@email.com");
        dto.setCep("01001000");
        dto.setNumero("10");
        rest.exchange("/clientes", HttpMethod.POST, authHeader(dto), String.class);

        ResponseEntity<Cliente> resp = rest.exchange(
                "/clientes/cpfCNPJ/12345678909", HttpMethod.GET, authHeader(), Cliente.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNomeCompleto()).isEqualTo("Busca CPF");
    }

    @Test
    @DisplayName("GET /clientes/cpfCNPJ/{cpfCNPJ} — deve retornar 404 para CPF inexistente")
    void deveRetornar404ParaCpfInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/clientes/cpfCNPJ/00000000000", HttpMethod.GET, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================================
    // PUT /clientes/{id}
    // =========================================================================

    @Test
    @DisplayName("PUT /clientes/{id} — deve retornar 200 ao atualizar cliente")
    void deveAtualizarCliente() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Original");
        dto.setCpfCNPJ("98765432100");
        dto.setTelefone("11900000007");
        dto.setEmail("original@email.com");
        dto.setCep("01001000");
        dto.setNumero("15");
        String id = createAndReturnId(dto);

        CadastrarClienteDTO upd = new CadastrarClienteDTO();
        upd.setNomeCompleto("Atualizado");
        upd.setCpfCNPJ("98765432100");
        upd.setTelefone("11999999999");
        upd.setEmail("atualizado@email.com");
        upd.setCep("01001000");
        upd.setNumero("20");

        ResponseEntity<String> resp = rest.exchange(
                "/clientes/" + id, HttpMethod.PUT, authHeader(upd), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("Cliente atualizado com sucesso");

        ResponseEntity<Cliente> getResp = rest.exchange(
                "/clientes/" + id, HttpMethod.GET, authHeader(), Cliente.class);
        assertThat(getResp.getBody().getNomeCompleto()).isEqualTo("Atualizado");
    }

    @Test
    @DisplayName("PUT /clientes/{id} — deve retornar 404 ao atualizar ID inexistente")
    void deveRetornar404AoAtualizarIdInexistente() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Inexistente");
        dto.setCpfCNPJ("52998224725");
        dto.setTelefone("11900000000");
        dto.setEmail("inexistente@email.com");
        dto.setCep("01001000");
        dto.setNumero("0");

        ResponseEntity<String> resp = rest.exchange(
                "/clientes/" + UUID.randomUUID(), HttpMethod.PUT, authHeader(dto), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // =========================================================================
    // DELETE /clientes/{id}
    // =========================================================================

    @Test
    @DisplayName("DELETE /clientes/{id} — deve retornar 204 ao deletar cliente sem veículos")
    void deveDeletarCliente() {
        CadastrarClienteDTO dto = new CadastrarClienteDTO();
        dto.setNomeCompleto("Deletar");
        dto.setCpfCNPJ("01987654366");
        dto.setTelefone("11900000008");
        dto.setEmail("deletar@email.com");
        dto.setCep("01001000");
        dto.setNumero("25");
        String id = createAndReturnId(dto);

        ResponseEntity<Void> resp = rest.exchange(
                "/clientes/" + id, HttpMethod.DELETE, authHeader(), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResp = rest.exchange(
                "/clientes/" + id, HttpMethod.GET, authHeader(), String.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /clientes/{id} — deve retornar 404 ao deletar ID inexistente")
    void deveRetornar404AoDeletarIdInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/clientes/" + UUID.randomUUID(), HttpMethod.DELETE, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
