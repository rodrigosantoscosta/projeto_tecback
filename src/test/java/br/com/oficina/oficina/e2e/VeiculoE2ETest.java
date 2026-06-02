package br.com.oficina.oficina.e2e;

import br.com.oficina.oficina.config.TestConfig;
import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.dto.cliente.ClienteListaDTO;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.Veiculo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E2E — CRUD Veículos")
class VeiculoE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private String accessToken;
    private UUID clienteId;

    @BeforeAll
    void setUp() {
        CadastrarFuncionarioDTO funcDTO = new CadastrarFuncionarioDTO();
        funcDTO.setNome("Func Veiculo E2E");
        funcDTO.setCpfCNPJ("11122233396");
        funcDTO.setCargo("ADMIN");
        funcDTO.setTelefone("11977777777");
        funcDTO.setEmail("func.veiculo@oficina.com");
        funcDTO.setUsuario("func.veiculo");
        funcDTO.setSenha("senha123");

        ResponseEntity<Object> createResp = rest.postForEntity("/funcionarios", funcDTO, Object.class);
        if (createResp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Falha ao criar funcionario: " + createResp.getStatusCode());
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsuario("func.veiculo");
        loginReq.setSenha("senha123");
        ResponseEntity<AuthResponse> loginResp = rest.postForEntity("/auth/login", loginReq, AuthResponse.class);
        accessToken = loginResp.getBody().getAccessToken();

        CadastrarClienteDTO clienteDTO = new CadastrarClienteDTO();
        clienteDTO.setNomeCompleto("Cliente Veiculo E2E");
        clienteDTO.setCpfCNPJ("40808737430");
        clienteDTO.setTelefone("11966666666");
        clienteDTO.setEmail("cliente.veiculo@email.com");
        clienteDTO.setCep("01001000");
        clienteDTO.setNumero("100");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<CadastrarClienteDTO> clienteEntity = new HttpEntity<>(clienteDTO, headers);
        ResponseEntity<String> clienteResp = rest.exchange("/clientes", HttpMethod.POST, clienteEntity, String.class);
        if (clienteResp.getStatusCode() != HttpStatus.CREATED) {
            throw new IllegalStateException("Falha ao criar cliente: " + clienteResp.getStatusCode());
        }

        ResponseEntity<List<ClienteListaDTO>> listResp = rest.exchange(
                "/clientes", HttpMethod.GET, new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ClienteListaDTO>>() {});
        clienteId = listResp.getBody().stream()
                .filter(c -> c.getCpfCNPJ().equals("12345678909"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cliente nao encontrado apos criacao"))
                .getId();
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

    private CadastrarVeiculoDTO criarVeiculoDTO(String placa) {
        CadastrarVeiculoDTO dto = new CadastrarVeiculoDTO();
        dto.setPlaca(placa);
        dto.setModelo("Civic");
        dto.setMarca("Honda");
        dto.setAno(2020);
        dto.setCor("Preto");
        dto.setQuilometragem(50000.0);
        dto.setClienteId(clienteId);
        return dto;
    }

    private String createAndReturnId(CadastrarVeiculoDTO dto) {
        ResponseEntity<Veiculo> resp = rest.exchange(
                "/veiculos", HttpMethod.POST, authHeader(dto), Veiculo.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getId().toString();
    }

    @Test
    @DisplayName("POST /veiculos — deve criar veiculo com sucesso e retornar 201")
    void deveCriarVeiculo() {
        CadastrarVeiculoDTO dto = criarVeiculoDTO("ABC1D23");
        ResponseEntity<Veiculo> resp = rest.exchange(
                "/veiculos", HttpMethod.POST, authHeader(dto), Veiculo.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getPlaca()).isEqualTo("ABC1D23");
        assertThat(resp.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("POST /veiculos — deve retornar 400 para dados invalidos")
    void deveRetornar400ParaDadosInvalidos() {
        CadastrarVeiculoDTO dto = new CadastrarVeiculoDTO();

        ResponseEntity<String> resp = rest.exchange(
                "/veiculos", HttpMethod.POST, authHeader(dto), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /veiculos — deve retornar 403 sem token")
    void deveRetornar403SemToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        CadastrarVeiculoDTO dto = criarVeiculoDTO("DEF2E34");

        ResponseEntity<String> resp = rest.exchange(
                "/veiculos", HttpMethod.POST,
                new HttpEntity<>(dto, headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /veiculos — deve retornar 200 com lista de veiculos")
    void deveListarVeiculos() {
        String placa1 = "GHI3F45";
        createAndReturnId(criarVeiculoDTO(placa1));

        CadastrarVeiculoDTO dto2 = criarVeiculoDTO("JKL4G56");
        createAndReturnId(dto2);

        ResponseEntity<List<Veiculo>> resp = rest.exchange(
                "/veiculos", HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<Veiculo>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).extracting("placa").contains(placa1, "JKL4G56");
    }

    @Test
    @DisplayName("GET /veiculos/{id} — deve retornar 200 com dados do veiculo")
    void deveBuscarPorId() {
        String id = createAndReturnId(criarVeiculoDTO("MNO5H67"));

        ResponseEntity<Veiculo> resp = rest.exchange(
                "/veiculos/" + id, HttpMethod.GET, authHeader(), Veiculo.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getPlaca()).isEqualTo("MNO5H67");
    }

    @Test
    @DisplayName("GET /veiculos/{id} — deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/veiculos/" + UUID.randomUUID(), HttpMethod.GET, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /veiculos/placa/{placa} — deve retornar 200 com dados do veiculo")
    void deveBuscarPorPlaca() {
        createAndReturnId(criarVeiculoDTO("PQR6I78"));

        ResponseEntity<Veiculo> resp = rest.exchange(
                "/veiculos/placa/PQR6I78", HttpMethod.GET, authHeader(), Veiculo.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getPlaca()).isEqualTo("PQR6I78");
    }

    @Test
    @DisplayName("GET /veiculos/placa/{placa} — deve retornar 404 para placa inexistente")
    void deveRetornar404ParaPlacaInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/veiculos/placa/ZZZ9999", HttpMethod.GET, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /veiculos/cliente/{clienteId} — deve retornar 200 com veiculos do cliente")
    void deveListarPorCliente() {
        createAndReturnId(criarVeiculoDTO("STU7J89"));

        ResponseEntity<List<Veiculo>> resp = rest.exchange(
                "/veiculos/cliente/" + clienteId, HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<Veiculo>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /veiculos/total-veiculos — deve retornar 200 com total")
    void deveContarTotalVeiculos() {
        ResponseEntity<Long> resp = rest.exchange(
                "/veiculos/total-veiculos", HttpMethod.GET, authHeader(), Long.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("PUT /veiculos/{id} — deve retornar 200 ao atualizar veiculo")
    void deveAtualizarVeiculo() {
        String id = createAndReturnId(criarVeiculoDTO("VWX8K90"));

        CadastrarVeiculoDTO upd = criarVeiculoDTO("VWX8K90");
        upd.setModelo("Corolla");
        upd.setMarca("Toyota");

        ResponseEntity<Veiculo> resp = rest.exchange(
                "/veiculos/" + id, HttpMethod.PUT, authHeader(upd), Veiculo.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getModelo()).isEqualTo("Corolla");
        assertThat(resp.getBody().getMarca()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("PUT /veiculos/{id} — deve retornar 404 ao atualizar ID inexistente")
    void deveRetornar404AoAtualizarIdInexistente() {
        CadastrarVeiculoDTO dto = criarVeiculoDTO("YZA9L01");

        ResponseEntity<String> resp = rest.exchange(
                "/veiculos/" + UUID.randomUUID(), HttpMethod.PUT, authHeader(dto), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /veiculos/{id} — deve retornar 204 ao deletar veiculo")
    void deveDeletarPorId() {
        String id = createAndReturnId(criarVeiculoDTO("BCD0M12"));

        ResponseEntity<Void> resp = rest.exchange(
                "/veiculos/" + id, HttpMethod.DELETE, authHeader(), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResp = rest.exchange(
                "/veiculos/" + id, HttpMethod.GET, authHeader(), String.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /veiculos/{id} — deve retornar 404 ao deletar ID inexistente")
    void deveRetornar404AoDeletarIdInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/veiculos/" + UUID.randomUUID(), HttpMethod.DELETE, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /veiculos/placa/{placa} — deve retornar 204 ao deletar por placa")
    void deveDeletarPorPlaca() {
        String placa = "EFG1N23";
        createAndReturnId(criarVeiculoDTO(placa));

        ResponseEntity<Void> resp = rest.exchange(
                "/veiculos/placa/" + placa, HttpMethod.DELETE, authHeader(), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResp = rest.exchange(
                "/veiculos/placa/" + placa, HttpMethod.GET, authHeader(), String.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /veiculos/placa/{placa} — deve retornar 404 para placa inexistente")
    void deveRetornar404AoDeletarPlacaInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/veiculos/placa/ZZZ9999", HttpMethod.DELETE, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /veiculos — deve retornar 409 para placa duplicada")
    void deveRetornar409ParaPlacaDuplicada() {
        String placa = "KLM0N12";
        createAndReturnId(criarVeiculoDTO(placa));

        ResponseEntity<String> resp = rest.exchange(
                "/veiculos", HttpMethod.POST, authHeader(criarVeiculoDTO(placa)), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("DELETE /veiculos/placa/{placa} — deve retornar 403 sem token")
    void deveRetornar403SemTokenAoDeletarPorPlaca() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = rest.exchange(
                "/veiculos/placa/ABC1D23", HttpMethod.DELETE,
                new HttpEntity<>(null, headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
