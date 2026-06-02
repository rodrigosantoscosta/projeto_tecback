package br.com.oficina.oficina.e2e;

import br.com.oficina.oficina.config.TestConfig;
import br.com.oficina.oficina.dto.atendimento.AtendimentoDTO;
import br.com.oficina.oficina.dto.atendimento.CadastrarAtendimentoDTO;
import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.dto.cliente.ClienteListaDTO;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.StatusAtendimento;
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
@DisplayName("E2E — CRUD Atendimentos")
class AtendimentoE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private String accessToken;
    private UUID clienteId;
    private String veiculoPlaca;
    private UUID funcionarioId;

    @BeforeAll
    void setUp() {
        CadastrarFuncionarioDTO funcDTO = new CadastrarFuncionarioDTO();
        funcDTO.setNome("Func Atendimento E2E");
        funcDTO.setCpfCNPJ("22233344405");
        funcDTO.setCargo("ADMIN");
        funcDTO.setTelefone("11955555555");
        funcDTO.setEmail("func.atendimento@oficina.com");
        funcDTO.setUsuario("func.atendimento");
        funcDTO.setSenha("senha123");

        ResponseEntity<FuncionarioDTO> funcResp = rest.postForEntity("/funcionarios", funcDTO, FuncionarioDTO.class);
        if (funcResp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Falha ao criar funcionario: " + funcResp.getStatusCode());
        }
        funcionarioId = funcResp.getBody().getId();

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsuario("func.atendimento");
        loginReq.setSenha("senha123");
        ResponseEntity<AuthResponse> loginResp = rest.postForEntity("/auth/login", loginReq, AuthResponse.class);
        accessToken = loginResp.getBody().getAccessToken();

        CadastrarClienteDTO clienteDTO = new CadastrarClienteDTO();
        clienteDTO.setNomeCompleto("Cliente Atendimento E2E");
        clienteDTO.setCpfCNPJ("52998224725");
        clienteDTO.setTelefone("11944444444");
        clienteDTO.setEmail("cliente.atendimento@email.com");
        clienteDTO.setCep("01001000");
        clienteDTO.setNumero("200");

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
                .filter(c -> c.getCpfCNPJ().equals("52998224725"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cliente nao encontrado apos criacao"))
                .getId();

        CadastrarVeiculoDTO veiculoDTO = new CadastrarVeiculoDTO();
        veiculoDTO.setPlaca("ATC1E23");
        veiculoDTO.setModelo("Gol");
        veiculoDTO.setMarca("Volkswagen");
        veiculoDTO.setAno(2019);
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

    private CadastrarAtendimentoDTO criarAtendimentoDTO() {
        CadastrarAtendimentoDTO dto = new CadastrarAtendimentoDTO();
        dto.setDescricaoServico("Troca de oleo e filtro");
        dto.setClienteId(clienteId);
        dto.setVeiculoPlaca(veiculoPlaca);
        dto.setFuncionarioId(funcionarioId);
        return dto;
    }

    private String createAndReturnId() {
        CadastrarAtendimentoDTO dto = criarAtendimentoDTO();
        ResponseEntity<AtendimentoDTO> resp = rest.exchange(
                "/atendimentos/cadastrar", HttpMethod.POST, authHeader(dto), AtendimentoDTO.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getId().toString();
    }

    @Test
    @DisplayName("POST /atendimentos/cadastrar — deve criar atendimento com sucesso e retornar 201")
    void deveCriarAtendimento() {
        CadastrarAtendimentoDTO dto = criarAtendimentoDTO();

        ResponseEntity<AtendimentoDTO> resp = rest.exchange(
                "/atendimentos/cadastrar", HttpMethod.POST, authHeader(dto), AtendimentoDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getId()).isNotNull();
        assertThat(resp.getBody().getDescricaoServico()).isEqualTo("Troca de oleo e filtro");
        assertThat(resp.getBody().getStatus()).isEqualTo(StatusAtendimento.AGUARDANDO);
    }

    @Test
    @DisplayName("POST /atendimentos/cadastrar — deve retornar 400 para dados invalidos")
    void deveRetornar400ParaDadosInvalidos() {
        CadastrarAtendimentoDTO dto = new CadastrarAtendimentoDTO();

        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/cadastrar", HttpMethod.POST, authHeader(dto), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /atendimentos/cadastrar — deve retornar 403 sem token")
    void deveRetornar403SemToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        CadastrarAtendimentoDTO dto = criarAtendimentoDTO();

        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/cadastrar", HttpMethod.POST,
                new HttpEntity<>(dto, headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /atendimentos/listar-todos — deve retornar 200 com lista")
    void deveListarTodos() {
        createAndReturnId();

        ResponseEntity<List<AtendimentoDTO>> resp = rest.exchange(
                "/atendimentos/listar-todos", HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<AtendimentoDTO>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /atendimentos/id/{id} — deve retornar 200 com dados do atendimento")
    void deveBuscarPorId() {
        String id = createAndReturnId();

        ResponseEntity<AtendimentoDTO> resp = rest.exchange(
                "/atendimentos/id/" + id, HttpMethod.GET, authHeader(), AtendimentoDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getId().toString()).isEqualTo(id);
    }

    @Test
    @DisplayName("GET /atendimentos/id/{id} — deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() {
        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/id/" + UUID.randomUUID(), HttpMethod.GET, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /atendimentos/cliente ID/{clienteId} — deve retornar 200 com lista")
    void deveListarPorCliente() {
        createAndReturnId();

        ResponseEntity<List<AtendimentoDTO>> resp = rest.exchange(
                "/atendimentos/cliente ID/" + clienteId, HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<AtendimentoDTO>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /atendimentos/listar-ordem-decrescente — deve retornar 200")
    void deveListarOrdemDecrescente() {
        createAndReturnId();

        ResponseEntity<List<AtendimentoDTO>> resp = rest.exchange(
                "/atendimentos/listar-ordem-decrescente", HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<AtendimentoDTO>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /atendimentos/listar-concluidos — deve retornar 200")
    void deveListarConcluidos() {
        ResponseEntity<List<AtendimentoDTO>> resp = rest.exchange(
                "/atendimentos/listar-concluidos", HttpMethod.GET, authHeader(),
                new ParameterizedTypeReference<List<AtendimentoDTO>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
    }

    @Test
    @DisplayName("PUT /atendimentos/atualizar/{id} — deve retornar 200 ao atualizar")
    void deveAtualizarAtendimento() {
        String id = createAndReturnId();

        CadastrarAtendimentoDTO upd = criarAtendimentoDTO();
        upd.setDescricaoServico("Troca de oleo, filtro e velas");

        ResponseEntity<AtendimentoDTO> resp = rest.exchange(
                "/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(upd), AtendimentoDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getDescricaoServico()).isEqualTo("Troca de oleo, filtro e velas");
    }

    @Test
    @DisplayName("DELETE /atendimentos/delete/{id} — deve retornar 200 ao deletar")
    void deveDeletarAtendimento() {
        String id = createAndReturnId();

        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/delete/" + id, HttpMethod.DELETE, authHeader(), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("sucesso");
    }

    @Test
    @DisplayName("PUT /atendimentos/atualizar/{id} — deve transicionar status valido (AGUARDANDO → ANDAMENTO)")
    void deveTransicionarStatusValido() {
        String id = createAndReturnId();

        CadastrarAtendimentoDTO upd = criarAtendimentoDTO();
        upd.setStatusAtendimento(StatusAtendimento.ANDAMENTO);

        ResponseEntity<AtendimentoDTO> resp = rest.exchange(
                "/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(upd), AtendimentoDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getStatus()).isEqualTo(StatusAtendimento.ANDAMENTO);
    }

    @Test
    @DisplayName("PUT /atendimentos/atualizar/{id} — deve retornar 422 para transicao invalida (AGUARDANDO → CONCLUIDO)")
    void deveRetornar422ParaTransicaoInvalida() {
        String id = createAndReturnId();

        CadastrarAtendimentoDTO upd = criarAtendimentoDTO();
        upd.setStatusAtendimento(StatusAtendimento.CONCLUIDO);

        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(upd), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("PUT /atendimentos/atualizar/{id} — deve transicionar ANDAMENTO → CONCLUIDO")
    void deveTransicionarAndamentoParaConcluido() {
        String id = createAndReturnId();

        CadastrarAtendimentoDTO paraAndamento = criarAtendimentoDTO();
        paraAndamento.setStatusAtendimento(StatusAtendimento.ANDAMENTO);
        rest.exchange("/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(paraAndamento), AtendimentoDTO.class);

        CadastrarAtendimentoDTO paraConcluido = criarAtendimentoDTO();
        paraConcluido.setStatusAtendimento(StatusAtendimento.CONCLUIDO);
        ResponseEntity<AtendimentoDTO> resp = rest.exchange(
                "/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(paraConcluido), AtendimentoDTO.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);
    }

    @Test
    @DisplayName("PUT /atendimentos/atualizar/{id} — deve retornar 422 ao transicionar de CONCLUIDO")
    void deveRetornar422ParaTransicaoDeConcluido() {
        String id = createAndReturnId();

        CadastrarAtendimentoDTO paraAndamento = criarAtendimentoDTO();
        paraAndamento.setStatusAtendimento(StatusAtendimento.ANDAMENTO);
        rest.exchange("/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(paraAndamento), AtendimentoDTO.class);

        CadastrarAtendimentoDTO paraConcluido = criarAtendimentoDTO();
        paraConcluido.setStatusAtendimento(StatusAtendimento.CONCLUIDO);
        rest.exchange("/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(paraConcluido), AtendimentoDTO.class);

        CadastrarAtendimentoDTO voltar = criarAtendimentoDTO();
        voltar.setStatusAtendimento(StatusAtendimento.ANDAMENTO);
        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(voltar), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("PUT /atendimentos/atualizar/{id} — deve retornar 422 ao transicionar de CANCELADO")
    void deveRetornar422ParaTransicaoDeCancelado() {
        String id = createAndReturnId();

        CadastrarAtendimentoDTO paraCancelado = criarAtendimentoDTO();
        paraCancelado.setStatusAtendimento(StatusAtendimento.CANCELADO);
        rest.exchange("/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(paraCancelado), AtendimentoDTO.class);

        CadastrarAtendimentoDTO voltar = criarAtendimentoDTO();
        voltar.setStatusAtendimento(StatusAtendimento.ANDAMENTO);
        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/atualizar/" + id, HttpMethod.PUT, authHeader(voltar), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("PUT /atendimentos/atualizar/{id} — deve retornar 403 sem token")
    void deveRetornar403SemTokenAoAtualizar() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = rest.exchange(
                "/atendimentos/atualizar/1", HttpMethod.PUT,
                new HttpEntity<>(criarAtendimentoDTO(), headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
