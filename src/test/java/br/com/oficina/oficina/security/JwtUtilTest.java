package br.com.oficina.oficina.security;

import br.com.oficina.oficina.model.Funcionario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil — testes unitários")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // secret com 32+ chars para satisfazer HS256
    private static final String SECRET = "segredo-de-teste-com-32-chars-ok!";
    private static final long   EXPIRATION_MS = 900_000L; // 15 min

    // ── fixtures ─────────────────────────────────────────────────────────────

    private Funcionario funcionario;
    private UsuarioPrincipal principal;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",           SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpirationMs", EXPIRATION_MS);

        funcionario = new Funcionario();
        funcionario.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        funcionario.setNome("Maria Técnica");
        funcionario.setUsuario("maria.tec");
        funcionario.setSenhaHash("$2a$10$hash");
        funcionario.setCargo("MECANICO");
        funcionario.setCpfCNPJ("52998224725");

        principal = UsuarioPrincipal.fromFuncionario(funcionario);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // generateAccessToken
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test
        @DisplayName("deve gerar token não-nulo e não-vazio")
        void deveGerarTokenNaoNulo() {
            String token = jwtUtil.generateAccessToken(principal);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("deve embutir username correto no subject")
        void deveEmbutirUsernameNoSubject() {
            String token = jwtUtil.generateAccessToken(principal);
            assertThat(jwtUtil.extractUsername(token)).isEqualTo("maria.tec");
        }

        @Test
        @DisplayName("deve embutir claim 'id' com UUID do funcionário")
        void deveEmbutirClaimId() {
            String token = jwtUtil.generateAccessToken(principal);

            String idClaim = jwtUtil.extractClaim(token, c -> c.get("id", String.class));
            assertThat(idClaim).isEqualTo("11111111-1111-1111-1111-111111111111");
        }

        @Test
        @DisplayName("deve embutir claim 'cargo' com cargo do funcionário")
        void deveEmbutirClaimCargo() {
            String token = jwtUtil.generateAccessToken(principal);

            String cargoClaim = jwtUtil.<String>extractClaim(token, c -> c.get("cargo", String.class));
            assertThat(cargoClaim).isEqualTo("MECANICO");
        }

        @Test
        @DisplayName("deve definir expiração aproximadamente em 15 minutos")
        void deveDefinirExpiracaoEm15Minutos() {
            long antes = System.currentTimeMillis();
            String token = jwtUtil.generateAccessToken(principal);
            long depois = System.currentTimeMillis();

            long expMs = jwtUtil.extractExpiration(token).getTime();

            // expiração deve estar entre antes+15min e depois+15min (margem de 1s)
            assertThat(expMs).isBetween(antes + EXPIRATION_MS - 1_000, depois + EXPIRATION_MS + 1_000);
        }

        @Test
        @DisplayName("deve assinar com HS256 — header alg correto")
        void deveAssinarComHS256() {
            String token = jwtUtil.generateAccessToken(principal);

            // Inspeciona o header sem validar a assinatura
            String[] partes = token.split("\\.");
            String headerJson = new String(java.util.Base64.getUrlDecoder().decode(partes[0]));
            assertThat(headerJson).contains("\"HS256\"");
        }

        @Test
        @DisplayName("deve gerar tokens diferentes quando iat muda (aguarda 1s entre chamadas)")
        void deveGerarTokensDiferentesEmChamadasConsecutivas() throws InterruptedException {
            String token1 = jwtUtil.generateAccessToken(principal);
            Thread.sleep(1_100); // JWT usa iat em segundos — precisa de 1s completo
            String token2 = jwtUtil.generateAccessToken(principal);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException ao criar principal com cargo vazio")
        void deveLancarExcecaoComCargoVazio() {
            Funcionario semCargo = new Funcionario();
            semCargo.setId(UUID.randomUUID());
            semCargo.setUsuario("sem.cargo");
            semCargo.setSenhaHash("hash");
            semCargo.setCargo("");   // cargo vazio — SimpleGrantedAuthority rejeita
            semCargo.setCpfCNPJ("00000000000");

            // Spring exige authority não-vazia — comportamento esperado e desejado
            assertThatThrownBy(() -> UsuarioPrincipal.fromFuncionario(semCargo))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("granted authority");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // isTokenValid
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("deve retornar true para token válido e username correspondente")
        void deveRetornarTrueParaTokenValido() {
            String token = jwtUtil.generateAccessToken(principal);
            assertThat(jwtUtil.isTokenValid(token, principal)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando username do token não corresponde ao UserDetails")
        void deveRetornarFalseParaUsernameErrado() {
            String token = jwtUtil.generateAccessToken(principal);

            Funcionario outro = new Funcionario();
            outro.setId(UUID.randomUUID());
            outro.setUsuario("outro.usuario");
            outro.setSenhaHash("hash");
            outro.setCargo("ADMIN");
            outro.setCpfCNPJ("11111111111");

            UsuarioPrincipal outroP = UsuarioPrincipal.fromFuncionario(outro);
            assertThat(jwtUtil.isTokenValid(token, outroP)).isFalse();
        }

        @Test
        @DisplayName("deve lançar ExpiredJwtException para token expirado")
        void deveLancarExcecaoParaTokenExpirado() {
            // Emite token já expirado (expiration = -1s)
            ReflectionTestUtils.setField(jwtUtil, "accessExpirationMs", -1000L);
            String tokenExpirado = jwtUtil.generateAccessToken(principal);

            assertThatThrownBy(() -> jwtUtil.isTokenValid(tokenExpirado, principal))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("deve lançar exceção para token com assinatura adulterada")
        void deveLancarExcecaoParaAssinaturaInvalida() {
            String token = jwtUtil.generateAccessToken(principal);

            // Adultera a assinatura (última parte do JWT)
            String[] partes = token.split("\\.");
            String tokenAdulterado = partes[0] + "." + partes[1] + ".assinaturafalsa";

            assertThatThrownBy(() -> jwtUtil.isTokenValid(tokenAdulterado, principal))
                    .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
        }

        @Test
        @DisplayName("deve lançar exceção para token malformado")
        void deveLancarExcecaoParaTokenMalformado() {
            assertThatThrownBy(() -> jwtUtil.isTokenValid("nao.e.um.jwt.valido", principal))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("deve rejeitar token assinado com secret diferente")
        void deveRejeitarTokenDeOutroSecret() {
            // Gera token com secret diferente do configurado no jwtUtil
            var outroSecret = "outro-segredo-totalmente-diferente!!";
            var chave = Keys.hmacShaKeyFor(outroSecret.getBytes(StandardCharsets.UTF_8));
            String tokenExterno = Jwts.builder()
                    .setSubject("maria.tec")
                    .signWith(chave)
                    .compact();

            assertThatThrownBy(() -> jwtUtil.isTokenValid(tokenExterno, principal))
                    .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
        }
    }

    @Nested
    @DisplayName("extractClaim")
    class ExtractClaim {

        @Test
        @DisplayName("deve extrair subject corretamente")
        void deveExtrairSubject() {
            String token = jwtUtil.generateAccessToken(principal);
            String subject = jwtUtil.extractClaim(token, Claims::getSubject);
            assertThat(subject).isEqualTo("maria.tec");
        }

        @Test
        @DisplayName("deve extrair claim customizado 'id'")
        void deveExtrairClaimId() {
            String token = jwtUtil.generateAccessToken(principal);
            String idClaim = jwtUtil.<String>extractClaim(token, c -> c.get("id", String.class));
            assertThat(idClaim).isEqualTo("11111111-1111-1111-1111-111111111111");
        }

        @Test
        @DisplayName("deve extrair claim customizado 'cargo'")
        void deveExtrairClaimCargo() {
            String token = jwtUtil.generateAccessToken(principal);
            String cargoClaim = jwtUtil.<String>extractClaim(token, c -> c.get("cargo", String.class));
            assertThat(cargoClaim).isEqualTo("MECANICO");
        }
    }
}
