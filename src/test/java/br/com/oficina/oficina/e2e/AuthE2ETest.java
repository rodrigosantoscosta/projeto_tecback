package br.com.oficina.oficina.e2e;

import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.auth.RefreshRequest;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E2E — Módulo de Autenticação")
class AuthE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private static final String USUARIO = "admin.e2e";
    private static final String SENHA   = "senha123";

    @BeforeAll
    void setUpAll() {
        rest.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
        dto.setNome("Admin E2E");
        dto.setCpfCNPJ("52998224725");
        dto.setCargo("ADMIN");
        dto.setTelefone("11999999999");
        dto.setEmail("admin.e2e@oficina.com");
        dto.setUsuario(USUARIO);
        dto.setSenha(SENHA);

        ResponseEntity<FuncionarioDTO> resp = rest.postForEntity("/funcionarios", dto, FuncionarioDTO.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Falha ao criar funcionário: " + resp.getStatusCode());
        }
    }

    private AuthResponse login() {
        LoginRequest req = new LoginRequest();
        req.setUsuario(USUARIO);
        req.setSenha(SENHA);
        return rest.postForEntity("/auth/login", req, AuthResponse.class).getBody();
    }

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("deve retornar 200 com accessToken e refreshToken")
        void deveLoginComSucesso() {
            LoginRequest req = new LoginRequest();
            req.setUsuario(USUARIO);
            req.setSenha(SENHA);

            ResponseEntity<AuthResponse> resp = rest.postForEntity("/auth/login", req, AuthResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getAccessToken()).isNotBlank();
            assertThat(resp.getBody().getRefreshToken()).isNotBlank();
            assertThat(resp.getBody().getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("deve retornar 401 para credenciais inválidas")
        void deveRetornar401ParaCredenciaisInvalidas() {
            LoginRequest req = new LoginRequest();
            req.setUsuario(USUARIO);
            req.setSenha("senha-errada");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LoginRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<String> resp = rest.exchange("/auth/login", HttpMethod.POST, entity, String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("deve retornar 400 para dados inválidos (usuário em branco)")
        void deveRetornar400ParaDadosInvalidos() {
            LoginRequest req = new LoginRequest();
            req.setUsuario("");
            req.setSenha("senha123");

            ResponseEntity<String> resp = rest.postForEntity("/auth/login", req, String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh")
    class Refresh {

        @Test
        @DisplayName("deve retornar 200 com novo par de tokens")
        void deveRefreshComSucesso() {
            String refreshToken = login().getRefreshToken();

            RefreshRequest req = new RefreshRequest();
            req.setRefreshToken(refreshToken);

            ResponseEntity<AuthResponse> resp = rest.postForEntity("/auth/refresh", req, AuthResponse.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resp.getBody()).isNotNull();
            assertThat(resp.getBody().getAccessToken()).isNotBlank();
            assertThat(resp.getBody().getRefreshToken()).isNotBlank();
            assertThat(resp.getBody().getRefreshToken()).isNotEqualTo(refreshToken);
        }

        @Test
        @DisplayName("deve retornar 401 para token revogado (refresh já usado)")
        void deveRetornar401ParaTokenJaRotacionado() {
            String refreshToken = login().getRefreshToken();

            RefreshRequest req = new RefreshRequest();
            req.setRefreshToken(refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // primeiro refresh — funciona
            HttpEntity<RefreshRequest> entity1 = new HttpEntity<>(req, headers);
            rest.exchange("/auth/refresh", HttpMethod.POST, entity1, AuthResponse.class);

            // segundo refresh com o mesmo token — deve falhar
            HttpEntity<RefreshRequest> entity2 = new HttpEntity<>(req, headers);
            ResponseEntity<String> resp = rest.exchange("/auth/refresh", HttpMethod.POST, entity2, String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("deve retornar 400 para refresh token em branco")
        void deveRetornar400ParaTokenEmBranco() {
            RefreshRequest req = new RefreshRequest();
            req.setRefreshToken("");

            ResponseEntity<String> resp = rest.postForEntity("/auth/refresh", req, String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("POST /auth/logout")
    class Logout {

        @Test
        @DisplayName("deve retornar 204 No Content")
        void deveRetornar204() {
            String refreshToken = login().getRefreshToken();

            RefreshRequest req = new RefreshRequest();
            req.setRefreshToken(refreshToken);

            ResponseEntity<Void> resp = rest.postForEntity("/auth/logout", req, Void.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("após logout, refresh token não deve mais funcionar")
        void aposLogoutRefreshTokenDeveSerRejeitado() {
            String refreshToken = login().getRefreshToken();

            RefreshRequest logoutReq = new RefreshRequest();
            logoutReq.setRefreshToken(refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RefreshRequest> logoutEntity = new HttpEntity<>(logoutReq, headers);
            rest.exchange("/auth/logout", HttpMethod.POST, logoutEntity, Void.class);

            RefreshRequest refreshReq = new RefreshRequest();
            refreshReq.setRefreshToken(refreshToken);

            HttpEntity<RefreshRequest> refreshEntity = new HttpEntity<>(refreshReq, headers);
            ResponseEntity<String> resp = rest.exchange("/auth/refresh", HttpMethod.POST, refreshEntity, String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
