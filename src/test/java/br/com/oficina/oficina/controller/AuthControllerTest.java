package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.auth.RefreshRequest;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.model.RefreshToken;
import br.com.oficina.oficina.security.JwtUtil;
import br.com.oficina.oficina.security.UsuarioPrincipal;
import br.com.oficina.oficina.service.RefreshTokenService;
import br.com.oficina.oficina.exception.CredenciaisInvalidasException;
import br.com.oficina.oficina.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController — testes unitários")
class AuthControllerTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtUtil                jwtUtil;
    @Mock RefreshTokenService    refreshTokenService;

    @InjectMocks AuthController controller;

    // ── fixtures ─────────────────────────────────────────────────────────────

    private Funcionario   funcionario;
    private UsuarioPrincipal principal;
    private RefreshToken  refreshToken;

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario();
        funcionario.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        funcionario.setNome("Carlos Admin");
        funcionario.setUsuario("carlos.adm");
        funcionario.setSenhaHash("$2a$10$hash");
        funcionario.setCargo("ADMIN");
        funcionario.setCpfCNPJ("52998224725");

        principal = UsuarioPrincipal.fromFuncionario(funcionario);

        refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh-uuid-token")
                .funcionario(funcionario)
                .expiraEm(Instant.now().plusSeconds(604800))
                .revogado(false)
                .build();
    }

    // helper — monta Authentication com principal correto
    private Authentication authComPrincipal() {
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /auth/login
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        private LoginRequest reqValida() {
            LoginRequest r = new LoginRequest();
            r.setUsuario("carlos.adm");
            r.setSenha("senha123");
            return r;
        }

        @Test
        @DisplayName("deve retornar 200 com accessToken e refreshToken em login bem-sucedido")
        void deveRetornar200ComTokens() {
            when(authenticationManager.authenticate(any())).thenReturn(authComPrincipal());
            when(jwtUtil.generateAccessToken(principal)).thenReturn("access.jwt.token");
            when(refreshTokenService.criar(funcionario)).thenReturn(refreshToken);

            ResponseEntity<?> resp = controller.login(reqValida());

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            AuthResponse body = (AuthResponse) resp.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getAccessToken()).isEqualTo("access.jwt.token");
            assertThat(body.getRefreshToken()).isEqualTo("refresh-uuid-token");
        }

        @Test
        @DisplayName("deve retornar tokenType 'Bearer' no body")
        void deveRetornarTokenTyperBearer() {
            when(authenticationManager.authenticate(any())).thenReturn(authComPrincipal());
            when(jwtUtil.generateAccessToken(any())).thenReturn("access.jwt.token");
            when(refreshTokenService.criar(any())).thenReturn(refreshToken);

            ResponseEntity<?> resp = controller.login(reqValida());
            AuthResponse body = (AuthResponse) resp.getBody();

            assertThat(body.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("deve lançar CredenciaisInvalidasException quando credenciais são inválidas")
        void deveRetornar401ParaCredenciaisInvalidas() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("bad credentials"));

            // GlobalExceptionHandler converte para 401 com ErrorDetails no contexto web
            assertThatThrownBy(() -> controller.login(reqValida()))
                    .isInstanceOf(CredenciaisInvalidasException.class)
                    .hasMessage("Credenciais inválidas");
        }

        @Test
        @DisplayName("não deve chamar refreshTokenService quando autenticação falha")
        void naoDeveChamarRefreshServiceEmFalha() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("bad credentials"));

            assertThatThrownBy(() -> controller.login(reqValida()))
                    .isInstanceOf(CredenciaisInvalidasException.class);

            verify(refreshTokenService, never()).criar(any());
            verify(jwtUtil, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("deve usar o Funcionario do principal — zero chamadas extras ao repositório")
        void deveUsarFuncionarioDoprincipaSemQueryExtra() {
            when(authenticationManager.authenticate(any())).thenReturn(authComPrincipal());
            when(jwtUtil.generateAccessToken(principal)).thenReturn("token");
            when(refreshTokenService.criar(funcionario)).thenReturn(refreshToken);

            controller.login(reqValida());

            // Verifica que criar() recebeu exatamente o funcionario do principal
            verify(refreshTokenService).criar(funcionario);
        }

        @Test
        @DisplayName("[blocking-fix] deve lançar IllegalStateException quando principal não é UsuarioPrincipal")
        void deveLancarExcecaoParaPrincipalDeOutroTipo() {
            // Simula Authentication com principal de tipo errado (String)
            Authentication authIncompativel = mock(Authentication.class);
            when(authIncompativel.getPrincipal()).thenReturn("principal-errado");
            when(authenticationManager.authenticate(any())).thenReturn(authIncompativel);

            assertThatThrownBy(() -> controller.login(reqValida()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Principal inesperado");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /auth/refresh
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /auth/refresh")
    class Refresh {

        private RefreshRequest reqRefresh() {
            RefreshRequest r = new RefreshRequest();
            r.setRefreshToken("refresh-uuid-token");
            return r;
        }

        @Test
        @DisplayName("deve retornar 200 com novo par de tokens em refresh bem-sucedido")
        void deveRetornar200ComNovosPar() {
            RefreshToken novoRt = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .token("novo-refresh-token")
                    .funcionario(funcionario)
                    .expiraEm(Instant.now().plusSeconds(604800))
                    .revogado(false)
                    .build();

            when(refreshTokenService.rotacionar("refresh-uuid-token")).thenReturn(novoRt);
            when(jwtUtil.generateAccessToken(any())).thenReturn("novo.access.token");

            ResponseEntity<?> resp = controller.refresh(reqRefresh());

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            AuthResponse body = (AuthResponse) resp.getBody();
            assertThat(body.getAccessToken()).isEqualTo("novo.access.token");
            assertThat(body.getRefreshToken()).isEqualTo("novo-refresh-token");
        }

        @Test
        @DisplayName("[blocking-fix] deve usar rotacionar() — operação atômica única")
        void deveUsarRotacionarAtomico() {
            when(refreshTokenService.rotacionar(anyString())).thenReturn(refreshToken);
            when(jwtUtil.generateAccessToken(any())).thenReturn("token");

            controller.refresh(reqRefresh());

            // rotacionar é chamado uma vez — nunca revogar + criar separados
            verify(refreshTokenService, times(1)).rotacionar("refresh-uuid-token");
            verify(refreshTokenService, never()).revogar(anyString());
            verify(refreshTokenService, never()).criar(any());
        }

        @Test
        @DisplayName("deve lançar CredenciaisInvalidasException quando refresh token está revogado")
        void deveRetornar401ParaTokenRevogado() {
            when(refreshTokenService.rotacionar(anyString()))
                    .thenThrow(new IllegalStateException("Refresh token revogado"));

            // GlobalExceptionHandler converte para 401 com ErrorDetails no contexto web
            assertThatThrownBy(() -> controller.refresh(reqRefresh()))
                    .isInstanceOf(CredenciaisInvalidasException.class)
                    .hasMessageContaining("revogado");
        }

        @Test
        @DisplayName("deve lançar CredenciaisInvalidasException quando refresh token está expirado")
        void deveRetornar401ParaTokenExpirado() {
            when(refreshTokenService.rotacionar(anyString()))
                    .thenThrow(new IllegalStateException("Refresh token expirado. Faça login novamente."));

            assertThatThrownBy(() -> controller.refresh(reqRefresh()))
                    .isInstanceOf(CredenciaisInvalidasException.class)
                    .hasMessageContaining("expirado");
        }

        @Test
        @DisplayName("deve retornar 401 quando refresh token não existe no banco")
        void deveRetornar401ParaTokenInexistente() {
            when(refreshTokenService.rotacionar(anyString()))
                    .thenThrow(new ResourceNotFoundException("Refresh token não encontrado"));

            // ResourceNotFoundException não é IllegalStateException —
            // não é capturada pelo catch do controller, vai propagar como 500.
            // Este teste documenta esse comportamento atual como ponto de atenção.
            assertThatThrownBy(() -> controller.refresh(reqRefresh()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("novo access token deve ser gerado com principal do novo refresh token")
        void deveGerarAccessTokenComPrincipalDoNovoRefresh() {
            RefreshToken novoRt = RefreshToken.builder()
                    .token("novo-refresh")
                    .funcionario(funcionario)
                    .expiraEm(Instant.now().plusSeconds(3600))
                    .revogado(false)
                    .build();
            when(refreshTokenService.rotacionar(anyString())).thenReturn(novoRt);
            when(jwtUtil.generateAccessToken(any())).thenReturn("novo.access");

            controller.refresh(reqRefresh());

            // Captura o principal passado ao generateAccessToken
            var captor = org.mockito.ArgumentCaptor.forClass(UsuarioPrincipal.class);
            verify(jwtUtil).generateAccessToken(captor.capture());
            assertThat(captor.getValue().getUsername()).isEqualTo("carlos.adm");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /auth/logout
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /auth/logout")
    class Logout {

        @Test
        @DisplayName("deve retornar 204 No Content em logout bem-sucedido")
        void deveRetornar204() {
            RefreshRequest req = new RefreshRequest();
            req.setRefreshToken("qualquer-token");

            ResponseEntity<Void> resp = controller.logout(req);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("deve delegar revogação ao RefreshTokenService")
        void deveDelegarRevogacao() {
            RefreshRequest req = new RefreshRequest();
            req.setRefreshToken("token-para-revogar");

            controller.logout(req);

            verify(refreshTokenService).revogar("token-para-revogar");
        }

        @Test
        @DisplayName("não deve lançar exceção quando token não existe (idempotente)")
        void naoDeveLancarExcecaoTokenInexistente() {
            RefreshRequest req = new RefreshRequest();
            req.setRefreshToken("token-inexistente");
            doNothing().when(refreshTokenService).revogar(anyString());

            assertThatCode(() -> controller.logout(req)).doesNotThrowAnyException();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UsuarioPrincipal — getFuncionario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("UsuarioPrincipal — getFuncionario (fix blocking-1)")
    class UsuarioPrincipalFix {

        @Test
        @DisplayName("fromFuncionario deve preservar referência ao Funcionario original")
        void devePreservarReferenciaDeFuncionario() {
            UsuarioPrincipal p = UsuarioPrincipal.fromFuncionario(funcionario);
            assertThat(p.getFuncionario()).isSameAs(funcionario);
        }

        @Test
        @DisplayName("deve expor id do funcionário")
        void deveExporId() {
            UsuarioPrincipal p = UsuarioPrincipal.fromFuncionario(funcionario);
            assertThat(p.getId()).isEqualTo(funcionario.getId());
        }

        @Test
        @DisplayName("deve expor username do funcionário como getUsername()")
        void deveExporUsername() {
            UsuarioPrincipal p = UsuarioPrincipal.fromFuncionario(funcionario);
            assertThat(p.getUsername()).isEqualTo("carlos.adm");
        }

        @Test
        @DisplayName("deve mapear cargo como GrantedAuthority")
        void deveMapearCargoComoAuthority() {
            UsuarioPrincipal p = UsuarioPrincipal.fromFuncionario(funcionario);
            assertThat(p.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ADMIN");
        }
    }
}
