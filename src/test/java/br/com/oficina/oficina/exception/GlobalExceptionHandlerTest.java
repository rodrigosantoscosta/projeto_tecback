package br.com.oficina.oficina.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para GlobalExceptionHandler.
 *
 * <p>Esta suite documenta comportamentos criticos de seguranca e regras de negocio
 * no tratamento de excecoes, incluindo mascaramento de mensagens internas
 * e prevencao de information leakage.</p>
 */
@DisplayName("GlobalExceptionHandler — testes unitarios de tratamento de erro")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/teste");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DataIntegrityViolationException — REDE DE SEGURANCA
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("handleDataIntegrityViolation")
    class DataIntegrityViolation {

        @Test
        @DisplayName("[SEGURANCA] deve retornar mensagem generica sem expor detalhes do banco")
        void deveMascararDetalhesDoBanco() {
            // Arrange: mensagem real do PostgreSQL com nome de constraint e tabela
            String mensagemInterna =
                "could not execute statement [ERROR: update or delete on table \"funcionarios\" " +
                "violates foreign key constraint \"fk_atendimento_funcionario\" on table \"atendimentos\". " +
                "Detail: Key (id)=(b2000000-0000-0000-0000-000000000001) is still referenced]";
            DataIntegrityViolationException ex = new DataIntegrityViolationException(mensagemInterna);

            // Act
            ResponseEntity<ErrorDetails> resp = handler.handleDataIntegrityViolation(ex, request);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(resp.getBody().getMensagem())
                .isEqualTo("Operação não permitida: registro possui vínculos com outros dados")
                .doesNotContain("funcionarios")
                .doesNotContain("fk_atendimento_funcionario")
                .doesNotContain("b2000000");
            assertThat(resp.getBody().getPath()).isEqualTo("/api/teste");
        }

        @Test
        @DisplayName("[SEGURANCA] deve retornar 409 mesmo quando mensagem interna eh nula")
        void deveRetornar409ParaMensagemNula() {
            DataIntegrityViolationException ex = new DataIntegrityViolationException(null);

            ResponseEntity<ErrorDetails> resp = handler.handleDataIntegrityViolation(ex, request);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(resp.getBody().getMensagem()).contains("vínculos");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // RuntimeException — INFORMATION LEAKAGE (VULNERABILIDADE DOCUMENTADA)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("handleRuntimeException")
    class RuntimeExceptionHandler {

        @Test
        @DisplayName("[VULNERABILIDADE] expoe mensagem interna diretamente — information leakage")
        void documentaVulnerabilidadeInformationLeakage() {
            // Arrange: RuntimeException com mensagem que pode conter detalhes internos
            RuntimeException ex = new RuntimeException(
                "NullPointerException at br.com.oficina.oficina.service.ClienteService:127 " +
                "while processing SQL [select * from senhas where ...]"
            );

            // Act
            ResponseEntity<ErrorDetails> resp = handler.handleRuntimeException(ex, request);

            // Assert: DOCUMENTA que a mensagem eh exposta diretamente (nao deve ser assim em prod)
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(resp.getBody().getMensagem())
                .contains("NullPointerException")
                .contains("br.com.oficina.oficina.service.ClienteService:127")
                .contains("select * from senhas");
        }

        @Test
        @DisplayName("deve retornar 500 para RuntimeException generico")
        void deveRetornar500() {
            ResponseEntity<ErrorDetails> resp =
                handler.handleRuntimeException(new RuntimeException("erro generico"), request);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Exception (catch-all absoluto) — MASCARAMENTO CORRETO
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("handleException")
    class ExceptionCatchAll {

        @Test
        @DisplayName("[SEGURANCA] deve mascarar mensagem generica para qualquer Exception")
        void deveMascararMensagem() {
            Exception ex = new Exception("Senha do banco: postgres://admin:secret@db:5432/oficina");

            ResponseEntity<ErrorDetails> resp = handler.handleException(ex, request);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(resp.getBody().getMensagem()).isEqualTo("Erro interno do servidor");
            assertThat(resp.getBody().getMensagem())
                .doesNotContain("Senha do banco")
                .doesNotContain("secret");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Regras de Negocio — Excecoes Customizadas
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("handleFuncionarioComAtendimentos")
    class FuncionarioComAtendimentos {

        @Test
        @DisplayName("deve retornar 409 com mensagem da regra de negocio")
        void deveRetornar409() {
            FuncionarioComAtendimentosException ex =
                new FuncionarioComAtendimentosException("Funcionario possui 3 atendimentos");

            ResponseEntity<ErrorDetails> resp = handler.handleFuncionarioComAtendimentos(ex, request);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(resp.getBody().getMensagem()).isEqualTo("Funcionario possui 3 atendimentos");
        }
    }

    @Nested
    @DisplayName("handleClienteComVeiculos")
    class ClienteComVeiculos {

        @Test
        @DisplayName("deve retornar 409 com mensagem da regra de negocio")
        void deveRetornar409() {
            ClienteComVeiculosException ex =
                new ClienteComVeiculosException("Cliente possui 2 veiculos");

            ResponseEntity<ErrorDetails> resp = handler.handleClienteComVeiculos(ex, request);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(resp.getBody().getMensagem()).isEqualTo("Cliente possui 2 veiculos");
        }
    }

    @Nested
    @DisplayName("handleTransicaoStatusInvalida")
    class TransicaoStatusInvalida {

        @Test
        @DisplayName("deve retornar 422 UNPROCESSABLE_ENTITY")
        void deveRetornar422() {
            TransicaoStatusInvalidaException ex =
                new TransicaoStatusInvalidaException("Transicao de CONCLUIDO para AGUARDANDO invalida");

            ResponseEntity<ErrorDetails> resp = handler.handleTransicaoStatusInvalida(ex, request);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(resp.getBody().getMensagem()).contains("invalida");
        }
    }

    @Nested
    @DisplayName("handleCredenciaisInvalidas")
    class CredenciaisInvalidas {

        @Test
        @DisplayName("deve retornar 401 UNAUTHORIZED")
        void deveRetornar401() {
            CredenciaisInvalidasException ex =
                new CredenciaisInvalidasException("Usuario ou senha invalidos");

            ResponseEntity<ErrorDetails> resp = handler.handleCredenciaisInvalidas(ex, request);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(resp.getBody().getMensagem()).contains("invalidos");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Validacao (MethodArgumentNotValidException)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("handleValidationExceptions")
    class Validation {

        @Test
        @DisplayName("deve retornar 400 com lista de erros de validacao")
        void deveRetornar400ComListaDeErros() {
            // Arrange: cria um binding result com erros de campo
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
                new Object(), "testeDTO"
            );
            bindingResult.addError(new FieldError("testeDTO", "email", "Email invalido"));
            bindingResult.addError(new FieldError("testeDTO", "cpf", "CPF obrigatorio"));
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            // Act
            ResponseEntity<ErrorDetails> resp = handler.handleValidationExceptions(ex, request);

            // Assert
            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resp.getBody().getMensagem()).contains("validação");
            assertThat(resp.getBody().getDetalhes())
                .hasSize(2)
                .contains("email: Email invalido", "cpf: CPF obrigatorio");
        }
    }
}
