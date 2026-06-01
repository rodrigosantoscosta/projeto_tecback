package br.com.oficina.oficina.service;

import br.com.oficina.oficina.exception.ResourceNotFoundException;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.model.RefreshToken;
import br.com.oficina.oficina.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService — testes unitários")
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @InjectMocks RefreshTokenService service;

    private static final long EXPIRATION_7_DIAS = 604_800_000L;

    // ── fixtures ─────────────────────────────────────────────────────────────

    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshExpirationMs", EXPIRATION_7_DIAS);

        funcionario = new Funcionario();
        funcionario.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        funcionario.setUsuario("joao.mec");
        funcionario.setSenhaHash("$2a$10$hash");
        funcionario.setCargo("MECANICO");
        funcionario.setCpfCNPJ("52998224725");
        funcionario.setNome("João Mecânico");
    }

    // helper — monta RefreshToken válido com expiração futura
    private RefreshToken tokenValido() {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID().toString())
                .funcionario(funcionario)
                .expiraEm(Instant.now().plusSeconds(3600))
                .revogado(false)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // criar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("deve revogar tokens anteriores antes de criar novo")
        void deveRevogarTokensAnteriores() {
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.criar(funcionario);

            verify(refreshTokenRepository).revogarTodosPorFuncionario(funcionario);
        }

        @Test
        @DisplayName("deve persistir token com funcionario correto")
        void devePersistirTokenComFuncionarioCorreto() {
            var captor = ArgumentCaptor.forClass(RefreshToken.class);
            when(refreshTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.criar(funcionario);

            RefreshToken salvo = captor.getValue();
            assertThat(salvo.getFuncionario()).isEqualTo(funcionario);
        }

        @Test
        @DisplayName("deve persistir token não-revogado")
        void devePersistirTokenNaoRevogado() {
            var captor = ArgumentCaptor.forClass(RefreshToken.class);
            when(refreshTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.criar(funcionario);

            assertThat(captor.getValue().isRevogado()).isFalse();
        }

        @Test
        @DisplayName("deve persistir token com expiração aproximadamente em 7 dias")
        void devePersistirTokenComExpiracaoCorreta() {
            var captor = ArgumentCaptor.forClass(RefreshToken.class);
            when(refreshTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            Instant antes  = Instant.now();
            service.criar(funcionario);
            Instant depois = Instant.now();

            Instant expiraEm = captor.getValue().getExpiraEm();
            assertThat(expiraEm).isBetween(
                    antes.plusMillis(EXPIRATION_7_DIAS  - 1_000),
                    depois.plusMillis(EXPIRATION_7_DIAS + 1_000)
            );
        }

        @Test
        @DisplayName("deve gerar token opaque (UUID format) a cada chamada")
        void deveGerarTokenUuid() {
            var captor = ArgumentCaptor.forClass(RefreshToken.class);
            when(refreshTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.criar(funcionario);

            String tokenStr = captor.getValue().getToken();
            // UUID tem 36 chars no formato xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
            assertThat(tokenStr).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("deve retornar o token persistido")
        void deveRetornarTokenPersistido() {
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken resultado = service.criar(funcionario);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getFuncionario()).isEqualTo(funcionario);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // validar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validar")
    class Validar {

        @Test
        @DisplayName("deve retornar token quando válido")
        void deveRetornarTokenValido() {
            RefreshToken rt = tokenValido();
            when(refreshTokenRepository.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

            RefreshToken resultado = service.validar(rt.getToken());

            assertThat(resultado).isEqualTo(rt);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando token não existe no banco")
        void deveLancarExcecaoTokenInexistente() {
            when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validar("token-que-nao-existe"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Refresh token não encontrado");
        }

        @Test
        @DisplayName("deve lançar IllegalStateException para token revogado")
        void deveLancarExcecaoTokenRevogado() {
            RefreshToken rt = tokenValido();
            rt.setRevogado(true);
            when(refreshTokenRepository.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

            assertThatThrownBy(() -> service.validar(rt.getToken()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("revogado");
        }

        @Test
        @DisplayName("deve lançar IllegalStateException para token expirado")
        void deveLancarExcecaoTokenExpirado() {
            RefreshToken rt = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .token(UUID.randomUUID().toString())
                    .funcionario(funcionario)
                    .expiraEm(Instant.now().minusSeconds(1)) // já expirou
                    .revogado(false)
                    .build();
            when(refreshTokenRepository.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

            assertThatThrownBy(() -> service.validar(rt.getToken()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("expirado");
        }

        @Test
        @DisplayName("[REGRA] deve checar revogado antes de expirado — token revogado e expirado lança 'revogado'")
        void deveChecarRevogadoAntesDeExpirado() {
            RefreshToken rt = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .token(UUID.randomUUID().toString())
                    .funcionario(funcionario)
                    .expiraEm(Instant.now().minusSeconds(1)) // expirado
                    .revogado(true)                          // e revogado
                    .build();
            when(refreshTokenRepository.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

            // Deve lançar "revogado" — a verificação de revogação vem primeiro no service
            assertThatThrownBy(() -> service.validar(rt.getToken()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("revogado");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // revogar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("revogar")
    class Revogar {

        @Test
        @DisplayName("deve marcar token como revogado e persistir")
        void deveMarcarRevogadoEPersistir() {
            RefreshToken rt = tokenValido();
            when(refreshTokenRepository.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

            service.revogar(rt.getToken());

            assertThat(rt.isRevogado()).isTrue();
            verify(refreshTokenRepository).save(rt);
        }

        @Test
        @DisplayName("não deve lançar exceção quando token não existe (idempotente)")
        void naoDeveLancarExcecaoParaTokenInexistente() {
            when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

            assertThatCode(() -> service.revogar("token-inexistente"))
                    .doesNotThrowAnyException();
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // rotacionar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("rotacionar")
    class Rotacionar {

        @Test
        @DisplayName("deve revogar token antigo e retornar novo token")
        void deveRevogarAntigoERetornarNovo() {
            RefreshToken rtAntigo = tokenValido();
            when(refreshTokenRepository.findByToken(rtAntigo.getToken())).thenReturn(Optional.of(rtAntigo));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken resultado = service.rotacionar(rtAntigo.getToken());

            assertThat(rtAntigo.isRevogado()).isTrue();
            assertThat(resultado).isNotNull();
            assertThat(resultado.isRevogado()).isFalse();
        }

        @Test
        @DisplayName("deve emitir novo token para o mesmo funcionário")
        void deveEmitirNovoTokenParaMesmoFuncionario() {
            RefreshToken rtAntigo = tokenValido();
            when(refreshTokenRepository.findByToken(rtAntigo.getToken())).thenReturn(Optional.of(rtAntigo));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken resultado = service.rotacionar(rtAntigo.getToken());

            assertThat(resultado.getFuncionario()).isEqualTo(funcionario);
        }

        @Test
        @DisplayName("novo token deve ser diferente do antigo")
        void novoTokenDeveSerDiferenteDoAntigo() {
            RefreshToken rtAntigo = tokenValido();
            String tokenAntigoStr = rtAntigo.getToken();
            when(refreshTokenRepository.findByToken(tokenAntigoStr)).thenReturn(Optional.of(rtAntigo));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken resultado = service.rotacionar(tokenAntigoStr);

            assertThat(resultado.getToken()).isNotEqualTo(tokenAntigoStr);
        }

        @Test
        @DisplayName("deve chamar revogarTodosPorFuncionario ao criar token novo (via criar interno)")
        void deveChamarRevogarTodosAoCriarNovo() {
            RefreshToken rtAntigo = tokenValido();
            when(refreshTokenRepository.findByToken(rtAntigo.getToken())).thenReturn(Optional.of(rtAntigo));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.rotacionar(rtAntigo.getToken());

            // revogarTodosPorFuncionario é chamado dentro de criar()
            verify(refreshTokenRepository).revogarTodosPorFuncionario(funcionario);
        }

        @Test
        @DisplayName("deve lançar IllegalStateException ao tentar rotacionar token já revogado")
        void deveLancarExcecaoParaTokenRevogado() {
            RefreshToken rtRevogado = tokenValido();
            rtRevogado.setRevogado(true);
            when(refreshTokenRepository.findByToken(rtRevogado.getToken())).thenReturn(Optional.of(rtRevogado));

            assertThatThrownBy(() -> service.rotacionar(rtRevogado.getToken()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("revogado");
            // nenhum novo token deve ter sido criado
            verify(refreshTokenRepository, never()).revogarTodosPorFuncionario(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException ao tentar rotacionar token inexistente")
        void deveLancarExcecaoParaTokenInexistente() {
            when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.rotacionar("token-fantasma"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
