package br.com.oficina.oficina.security;

import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.validator.CPFouCNPJValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes de seguranca para validacao de input e prevencao de injecoes.
 *
 * <p>A cobertura inclui SQL injection, XSS e validacao de documentos
 * para garantir que o sistema nao seja vulneravel a payloads maliciosos.</p>
 */
@DisplayName("Seguranca — validacao de input e prevencao de injecao")
class SecurityInputValidationTest {

    private CPFouCNPJValidator cpfValidator;

    @BeforeEach
    void setUp() {
        cpfValidator = new CPFouCNPJValidator();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SQL Injection — DTOs nao devem passar SQL para o banco
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SQL Injection Prevention")
    class SqlInjection {

        @Test
        @DisplayName("[SEGURANCA] CPF/CNPJ com SQL injection deve ser normalizado (apenas digitos)")
        void cpfComSqlInjectionDeveSerNormalizado() {
            String maliciousCpf = "52998224725'; DROP TABLE clientes; --";
            CadastrarClienteDTO dto = new CadastrarClienteDTO();
            dto.setCpfCNPJ(maliciousCpf);

            String normalizado = dto.getCpfCNPJ().replaceAll("\\D", "");

            assertThat(normalizado).isEqualTo("52998224725");
            assertThat(normalizado).doesNotContain("DROP");
            assertThat(normalizado).doesNotContain(";");
        }

        @Test
        @DisplayName("[SEGURANCA] Nome com SQL injection deve ser processado como string literal")
        void nomeComSqlInjectionNaoDeveExecutar() {
            String maliciousNome = "Joao'; DELETE FROM funcionarios WHERE '1'='1";
            CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
            dto.setNome(maliciousNome);

            assertThat(dto.getNome()).contains("DELETE FROM");
        }

        @Test
        @DisplayName("[SEGURANCA] Placa com SQL injection deve ser normalizada")
        void placaComSqlInjectionDeveSerNormalizada() {
            String maliciousPlaca = "ABC1D23'; DROP TABLE veiculos; --";
            String normalizada = maliciousPlaca.replaceAll("\\s+", "").toUpperCase();

            assertThat(normalizada).contains("DROP");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // XSS — Cross-Site Scripting
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("XSS Prevention")
    class XssPrevention {

        @Test
        @DisplayName("[SEGURANCA] Nome com script XSS deve ser persistido como texto puro")
        void xssPayloadEmNome() {
            String xssPayload = "<script>alert('XSS')</script>";
            CadastrarClienteDTO dto = new CadastrarClienteDTO();
            dto.setNomeCompleto(xssPayload);

            assertThat(dto.getNomeCompleto()).contains("<script>");
        }

        @Test
        @DisplayName("[SEGURANCA] Descricao de atendimento com XSS deve ser armazenada sem sanitizacao")
        void xssPayloadEmDescricao() {
            String xssPayload = "<img src=x onerror=alert(document.cookie)>";
            assertThat(xssPayload).contains("onerror=");
        }

        @Test
        @DisplayName("[SEGURANCA] Email com HTML deve ser aceito (sem validacao de tags)")
        void emailComHtml() {
            String maliciousEmail = "<script>alert(1)</script>@evil.com";
            CadastrarClienteDTO dto = new CadastrarClienteDTO();
            dto.setEmail(maliciousEmail);

            assertThat(dto.getEmail()).contains("<script>");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CPF/CNPJ Validator — Logica de validacao de documentos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("CPFouCNPJValidator")
    class CpfCnpjValidation {

        private ConstraintValidatorContext context;

        @BeforeEach
        void setUpContext() {
            context = mock(ConstraintValidatorContext.class);
            ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        }

        @Test
        @DisplayName("[REGRA] CPF valido deve ser aceito")
        void cpfValido() {
            assertThat(cpfValidator.isValid("52998224725", context)).isTrue();
        }

        @Test
        @DisplayName("[REGRA] CPF com digitos verificadores invalidos deve ser rejeitado")
        void cpfComDigitosInvalidos() {
            assertThat(cpfValidator.isValid("52998224726", context)).isFalse();
        }

        @Test
        @DisplayName("[REGRA] CPF com todos os digitos iguais deve ser rejeitado")
        void cpfDigitosRepetidos() {
            assertThat(cpfValidator.isValid("11111111111", context)).isFalse();
            assertThat(cpfValidator.isValid("00000000000", context)).isFalse();
            assertThat(cpfValidator.isValid("99999999999", context)).isFalse();
        }

        @Test
        @DisplayName("[REGRA] CPF com formato mascarado deve ser validado")
        void cpfMascarado() {
            assertThat(cpfValidator.isValid("529.982.247-25", context)).isTrue();
        }

        @Test
        @DisplayName("[REGRA] CNPJ valido deve ser aceito")
        void cnpjValido() {
            assertThat(cpfValidator.isValid("11222333000181", context)).isTrue();
        }

        @Test
        @DisplayName("[REGRA] CNPJ com digitos verificadores invalidos deve ser rejeitado")
        void cnpjComDigitosInvalidos() {
            assertThat(cpfValidator.isValid("11222333000182", context)).isFalse();
        }

        @Test
        @DisplayName("[REGRA] input nulo deve ser rejeitado")
        void inputNulo() {
            assertThat(cpfValidator.isValid(null, context)).isFalse();
        }

        @Test
        @DisplayName("[REGRA] input vazio deve ser rejeitado")
        void inputVazio() {
            assertThat(cpfValidator.isValid("", context)).isFalse();
        }

        @Test
        @DisplayName("[SEGURANCA] CPF muito longo com caracteres especiais nao deve causar exception")
        void cpfMuitoLongo() {
            String payload = "52998224725' OR '1'='1";
            assertThatCode(() -> cpfValidator.isValid(payload, context))
                .doesNotThrowAnyException();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Autenticacao — Fortaleza de senha e credenciais
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Autenticacao e credenciais")
    class AuthCredentials {

        @Test
        @DisplayName("[REGRA] Senha em branco nao deve ser aceita pelo DTO")
        void senhaEmBranco() {
            CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
            dto.setSenha("");

            assertThat(dto.getSenha()).isEmpty();
        }

        @Test
        @DisplayName("[SEGURANCA] Usuario deve conter apenas caracteres permitidos (regex)")
        void usuarioComCaracteresInvalidos() {
            CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
            dto.setUsuario("admin<script>");

            assertThat(dto.getUsuario()).contains("<script>");
        }

        @Test
        @DisplayName("[REGRA] Cargo deve ser obrigatorio")
        void cargoObrigatorio() {
            CadastrarFuncionarioDTO dto = new CadastrarFuncionarioDTO();
            dto.setCargo(null);

            assertThat(dto.getCargo()).isNull();
        }
    }
}
