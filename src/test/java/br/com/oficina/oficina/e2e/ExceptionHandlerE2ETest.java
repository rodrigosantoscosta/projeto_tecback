package br.com.oficina.oficina.e2e;

import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes E2E que validam o contrato de erro HTTP retornado pelo
 * GlobalExceptionHandler em cenarios reais de producao.
 *
 * <p>Documenta que o sistema retorna mensagens mascaradas para erros
 * de banco (409) mas ainda expoe detalhes em RuntimeException (500).</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E2E — Contrato de erro HTTP (GlobalExceptionHandler)")
class ExceptionHandlerE2ETest {

    @Autowired private TestRestTemplate rest;

    private String accessToken;
    private String testUsuario;

    @BeforeAll
    void setUp() {
        // Dados aleatorios para evitar conflito com execucoes anteriores no banco persistente
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testUsuario = "handler.e2e." + suffix;
        String cpf = gerarCpfValido();
        String email = "handler.e2e." + suffix + "@oficina.com";

        // Cria um funcionario para obter token
        CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
        dto.setNome("Auth Handler E2E");
        dto.setCpfCNPJ(cpf);
        dto.setCargo("ADMIN");
        dto.setTelefone("11933333333");
        dto.setEmail(email);
        dto.setUsuario(testUsuario);
        dto.setSenha("senha123");

        ResponseEntity<FuncionarioDTO> resp = rest.postForEntity("/funcionarios", dto, FuncionarioDTO.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Login para obter token
        br.com.oficina.oficina.dto.auth.LoginRequest login = new br.com.oficina.oficina.dto.auth.LoginRequest();
        login.setUsuario(testUsuario);
        login.setSenha("senha123");

        ResponseEntity<br.com.oficina.oficina.dto.auth.AuthResponse> loginResp =
            rest.postForEntity("/auth/login", login, br.com.oficina.oficina.dto.auth.AuthResponse.class);
        accessToken = loginResp.getBody().getAccessToken();
    }

    private HttpEntity<?> authHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(null, headers);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 401 Unauthorized
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /funcionarios/me sem token deve retornar 403 (Spring Security)")
    void deveRetornar403SemToken() {
        ResponseEntity<String> resp = rest.getForEntity("/funcionarios/me", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("POST /auth/login com credenciais invalidas deve retornar 401")
    void deveRetornar401CredenciaisInvalidas() {
        br.com.oficina.oficina.dto.auth.LoginRequest login = new br.com.oficina.oficina.dto.auth.LoginRequest();
        login.setUsuario("handler.e2e");
        login.setSenha("senha-errada");

        ResponseEntity<Map> resp = rest.postForEntity("/auth/login", login, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().get("mensagem")).asString().contains("inválidas");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 404 Not Found
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /funcionarios/{id} com ID inexistente deve retornar 404")
    void deveRetornar404IdInexistente() {
        ResponseEntity<Map> resp = rest.exchange(
            "/funcionarios/" + UUID.randomUUID(),
            HttpMethod.GET, authHeader(), Map.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().get("mensagem")).asString().contains("não encontrado");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 409 Conflict — Duplicidade
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /funcionarios com usuario duplicado deve retornar 409")
    void deveRetornar409UsuarioDuplicado() {
        CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
        dto.setNome("Duplicado");
        dto.setCpfCNPJ("88899911193");
        dto.setCargo("MECANICO");
        dto.setTelefone("11922222222");
        dto.setEmail("dup@oficina.com");
        dto.setUsuario(testUsuario); // ja existe
        dto.setSenha("senha123");

        ResponseEntity<Map> resp = rest.postForEntity("/funcionarios", dto, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().get("mensagem")).asString().contains("já cadastrado");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 409 Conflict — DataIntegrityViolationException (rede de seguranca)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[SEGURANCA] RuntimeException inesperado deve retornar 500 (documenta rede de seguranca)")
    void deveRetornar500ParaRuntimeException() {
        ResponseEntity<Map> resp = rest.exchange(
            "/test/force/runtime?msg=erro-interno-de-teste",
            HttpMethod.GET, authHeader(), Map.class
        );

        // Este endpoint forca um RuntimeException para documentar o comportamento
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 400 Bad Request — Validacao
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /funcionarios com dados invalidos deve retornar 400 com lista de erros")
    void deveRetornar400ComListaDeErros() {
        CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO(); // todos os campos nulos

        ResponseEntity<Map> resp = rest.postForEntity("/funcionarios", dto, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("detalhes")).isNotNull();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 500 Internal Server Error — RuntimeException (VULNERABILIDADE DOCUMENTADA)
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("[VULNERABILIDADE] RuntimeException inesperado expoe mensagem interna no body 500")
    void documentaInformationLeakageEm500() {
        String mensagemInterna = "Detalhes sensiveis: conexao com banco postgres://admin:secret@host/db";

        ResponseEntity<Map> resp = rest.exchange(
            "/test/force/runtime?msg=" + mensagemInterna,
            HttpMethod.GET, authHeader(), Map.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        // DOCUMENTA: a mensagem interna eh exposta diretamente no body
        assertThat(resp.getBody().get("mensagem")).asString().contains("Detalhes sensiveis");
        assertThat(resp.getBody().get("mensagem")).asString().contains("postgres://admin:secret");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Utilitários
    // ═════════════════════════════════════════════════════════════════════════

    private String gerarCpfValido() {
        java.util.Random rand = new java.util.Random();
        int[] cpf = new int[11];
        for (int i = 0; i < 9; i++) cpf[i] = rand.nextInt(10);
        // Calcula 1º dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += cpf[i] * (10 - i);
        int resto = soma % 11;
        cpf[9] = (resto < 2) ? 0 : 11 - resto;
        // Calcula 2º dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) soma += cpf[i] * (11 - i);
        resto = soma % 11;
        cpf[10] = (resto < 2) ? 0 : 11 - resto;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) sb.append(cpf[i]);
        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Controller de teste para forcar excecoes controladas
    // ═════════════════════════════════════════════════════════════════════════

    @TestConfiguration
    static class ExceptionTestControllerConfig {
        @Bean
        public ExceptionTestController exceptionTestController() {
            return new ExceptionTestController();
        }
    }

    @RestController
    static class ExceptionTestController {

        @GetMapping("/test/force/runtime")
        public void forceRuntimeException(@RequestParam String msg) {
            throw new RuntimeException(msg);
        }
    }
}
