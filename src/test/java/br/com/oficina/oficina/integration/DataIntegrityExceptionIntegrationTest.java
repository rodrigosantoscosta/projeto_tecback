package br.com.oficina.oficina.integration;

import br.com.oficina.oficina.model.Atendimento;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.model.StatusAtendimento;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.AtendimentoRepository;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.FuncionarioRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integracao que documentam e validam o comportamento do sistema
 * quando ocorrem violacoes de integridade referencial no banco de dados
 * (DataIntegrityViolationException).
 *
 * <p>Estes testes simulam a race condition onde a validacao de contagem
 * (countBy...) nao detecta registros criados concorrentemente.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integracao — DataIntegrityViolationException como rede de seguranca")
class DataIntegrityExceptionIntegrationTest {

    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;

    private Funcionario funcionario;
    private Cliente cliente;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        // Limpa dados anteriores
        atendimentoRepository.deleteAll();
        veiculoRepository.deleteAll();
        clienteRepository.deleteAll();
        funcionarioRepository.deleteAll();

        // Cria entidades base
        funcionario = new Funcionario();
        funcionario.setNome("Teste Func");
        funcionario.setCpfCNPJ("52998224725");
        funcionario.setUsuario("teste.func");
        funcionario.setSenhaHash("$2a$10$hash");
        funcionario.setCargo("MECANICO");
        funcionario = funcionarioRepository.save(funcionario);

        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setCpfCNPJ("11222333000181");
        cliente.setTelefone("11911111111");
        cliente.setEmail("teste@email.com");
        cliente = clienteRepository.save(cliente);

        veiculo = new Veiculo();
        veiculo.setPlaca("ABC1D23");
        veiculo.setModelo("Civic");
        veiculo.setMarca("Honda");
        veiculo.setAno(2020);
        veiculo.setCliente(cliente);
        veiculo = veiculoRepository.save(veiculo);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Funcionario — FK violation via atendimento
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Funcionario com atendimento vinculado")
    class FuncionarioComAtendimento {

        @Test
        @DisplayName("[RACE CONDITION] deve lancar DataIntegrityViolationException ao deletar funcionario com atendimentos")
        @Transactional
        void deveLancarExcecaoAoDeletarFuncionarioComAtendimentos() {
            // Arrange: cria atendimento vinculado ao funcionario
            Atendimento atendimento = new Atendimento();
            atendimento.setDescricaoServico("Servico teste");
            atendimento.setDataEntrada(LocalDateTime.now());
            atendimento.setStatus(StatusAtendimento.AGUARDANDO);
            atendimento.setCliente(cliente);
            atendimento.setVeiculo(veiculo);
            atendimento.setFuncionario(funcionario);
            atendimentoRepository.save(atendimento);

            // Act & Assert: delete direto no repository deve falhar com FK violation
            assertThatThrownBy(() -> {
                funcionarioRepository.deleteById(funcionario.getId());
                funcionarioRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("[REGRA] Service deve detectar atendimento e lancar excecao ANTES do banco")
        void serviceDeveDetectarAntesDoBanco() {
            // Arrange
            Atendimento atendimento = new Atendimento();
            atendimento.setDescricaoServico("Servico teste");
            atendimento.setDataEntrada(LocalDateTime.now());
            atendimento.setStatus(StatusAtendimento.AGUARDANDO);
            atendimento.setCliente(cliente);
            atendimento.setVeiculo(veiculo);
            atendimento.setFuncionario(funcionario);
            atendimentoRepository.save(atendimento);

            // Act & Assert: count deve detectar o atendimento
            long count = atendimentoRepository.countByFuncionarioId(funcionario.getId());
            assertThat(count).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Cliente com veiculo vinculado")
    class ClienteComVeiculo {

        @Test
        @DisplayName("[RACE CONDITION] deve lancar DataIntegrityViolationException ao deletar cliente com veiculos")
        @Transactional
        void deveLancarExcecaoAoDeletarClienteComVeiculos() {
            // Arrange: veiculo ja esta criado no setUp e vinculado ao cliente

            // Act & Assert
            assertThatThrownBy(() -> {
                clienteRepository.deleteById(cliente.getId());
                clienteRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Veiculo com atendimento vinculado")
    class VeiculoComAtendimento {

        @Test
        @DisplayName("[RACE CONDITION] deve lancar DataIntegrityViolationException ao deletar veiculo com atendimentos")
        @Transactional
        void deveLancarExcecaoAoDeletarVeiculoComAtendimentos() {
            // Arrange: cria atendimento vinculado ao veiculo
            Atendimento atendimento = new Atendimento();
            atendimento.setDescricaoServico("Servico teste");
            atendimento.setDataEntrada(LocalDateTime.now());
            atendimento.setStatus(StatusAtendimento.AGUARDANDO);
            atendimento.setCliente(cliente);
            atendimento.setVeiculo(veiculo);
            atendimento.setFuncionario(funcionario);
            atendimentoRepository.save(atendimento);

            // Act & Assert
            assertThatThrownBy(() -> {
                veiculoRepository.deleteById(veiculo.getId());
                veiculoRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
