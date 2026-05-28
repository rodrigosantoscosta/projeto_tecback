package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.atendimento.AtendimentoDTO;
import br.com.oficina.oficina.dto.atendimento.CadastrarAtendimentoDTO;
import br.com.oficina.oficina.exception.AtendimentoNaoEncontrado;
import br.com.oficina.oficina.exception.ClienteNaoEncontradoException;
import br.com.oficina.oficina.exception.VeiculoNaoEncontradoException;
import br.com.oficina.oficina.model.*;
import br.com.oficina.oficina.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtendimentoService — testes unitários")
class AtendimentoServiceTest {

    @Mock AtendimentoRepository atendimentoRepository;
    @Mock ClienteRepository     clienteRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @Mock VeiculoRepository     veiculoRepository;

    @InjectMocks
    AtendimentoService service;

    // ── fixtures ──────────────────────────────────────────────────────────────

    private Cliente     cliente;
    private Veiculo     veiculo;
    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNomeCompleto("João da Silva");
        cliente.setCpfCNPJ("52998224725");

        veiculo = new Veiculo();
        veiculo.setId(UUID.randomUUID());
        veiculo.setPlaca("ABC1D23");
        veiculo.setModelo("Civic");
        veiculo.setCliente(cliente);

        funcionario = new Funcionario();
        funcionario.setId(UUID.randomUUID());
        funcionario.setNome("Admin");
    }

    /** Monta um Atendimento já persistido com todos os relacionamentos. */
    private Atendimento atendimentoSalvo(StatusAtendimento status) {
        Atendimento a = new Atendimento();
        a.setId(UUID.randomUUID());
        a.setDescricaoServico("Troca de óleo");
        a.setStatus(status);
        a.setDataCadastro(LocalDateTime.now());
        a.setDataEntrada(LocalDateTime.now());
        a.setCliente(cliente);
        a.setVeiculo(veiculo);
        a.setFuncionario(funcionario);
        return a;
    }

    /** DTO de entrada padrão para criar/atualizar atendimento. */
    private CadastrarAtendimentoDTO dto(StatusAtendimento status) {
        CadastrarAtendimentoDTO d = new CadastrarAtendimentoDTO();
        d.setClienteId(cliente.getId());
        d.setVeiculoPlaca(veiculo.getPlaca());
        d.setFuncionarioId(funcionario.getId());
        d.setDescricaoServico("Troca de óleo");
        d.setStatusAtendimento(status);
        return d;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // cadastrarAtendimento
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cadastrarAtendimento")
    class CadastrarAtendimento {

        @Test
        @DisplayName("deve cadastrar atendimento com sucesso quando todos os recursos existem")
        void deveCadastrarComSucesso() {
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
            when(funcionarioRepository.findById(funcionario.getId())).thenReturn(Optional.of(funcionario));
            when(atendimentoRepository.save(any())).thenAnswer(inv -> {
                Atendimento a = inv.getArgument(0);
                a.setId(UUID.randomUUID());
                a.setDataCadastro(LocalDateTime.now());
                a.setDataEntrada(LocalDateTime.now());
                return a;
            });

            AtendimentoDTO resultado = service.cadastrarAtendimento(dto(StatusAtendimento.AGUARDANDO));

            assertThat(resultado).isNotNull();
            assertThat(resultado.getStatus()).isEqualTo(StatusAtendimento.AGUARDANDO);
            assertThat(resultado.getCliente()).isEqualTo(cliente.getId());
            assertThat(resultado.getVeiculo()).isEqualTo("ABC1D23");
            verify(atendimentoRepository).save(any(Atendimento.class));
        }

        @Test
        @DisplayName("deve lançar ClienteNaoEncontradoException quando cliente não existe")
        void deveLancarExcecaoClienteNaoEncontrado() {
            when(clienteRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cadastrarAtendimento(dto(StatusAtendimento.AGUARDANDO)))
                    .isInstanceOf(ClienteNaoEncontradoException.class)
                    .hasMessageContaining("Cliente não encontrado");
            verifyNoInteractions(atendimentoRepository);
        }

        @Test
        @DisplayName("deve lançar VeiculoNaoEncontradoException quando veículo não existe")
        void deveLancarExcecaoVeiculoNaoEncontrado() {
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cadastrarAtendimento(dto(StatusAtendimento.AGUARDANDO)))
                    .isInstanceOf(VeiculoNaoEncontradoException.class)
                    .hasMessageContaining("Veículo não encontrado");
            verifyNoInteractions(atendimentoRepository);
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando funcionário não existe")
        void deveLancarExcecaoFuncionarioNaoEncontrado() {
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
            when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cadastrarAtendimento(dto(StatusAtendimento.AGUARDANDO)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Funcionário não encontrado");
            verifyNoInteractions(atendimentoRepository);
        }

        @Test
        @DisplayName("deve preencher dataConclusao automaticamente ao cadastrar com status CONCLUIDO")
        void devePreencherDataConclusaoAoCadastrarComStatusConcluido() {
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
            when(funcionarioRepository.findById(funcionario.getId())).thenReturn(Optional.of(funcionario));
            when(atendimentoRepository.save(any())).thenAnswer(inv -> {
                Atendimento a = inv.getArgument(0);
                a.setId(UUID.randomUUID());
                a.setDataCadastro(LocalDateTime.now());
                a.setDataEntrada(LocalDateTime.now());
                return a;
            });

            AtendimentoDTO resultado = service.cadastrarAtendimento(dto(StatusAtendimento.CONCLUIDO));

            assertThat(resultado.getDataConclusao()).isNotNull();
        }

        @Test
        @DisplayName("deve preencher dataConclusao automaticamente ao cadastrar com status CANCELADO")
        void devePreencherDataConclusaoAoCadastrarComStatusCancelado() {
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
            when(funcionarioRepository.findById(funcionario.getId())).thenReturn(Optional.of(funcionario));
            when(atendimentoRepository.save(any())).thenAnswer(inv -> {
                Atendimento a = inv.getArgument(0);
                a.setId(UUID.randomUUID());
                a.setDataCadastro(LocalDateTime.now());
                a.setDataEntrada(LocalDateTime.now());
                return a;
            });

            AtendimentoDTO resultado = service.cadastrarAtendimento(dto(StatusAtendimento.CANCELADO));

            assertThat(resultado.getDataConclusao()).isNotNull();
        }

        @Test
        @DisplayName("não deve preencher dataConclusao quando status é AGUARDANDO")
        void naoDevePreencherDataConclusaoQuandoStatusAguardando() {
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
            when(funcionarioRepository.findById(funcionario.getId())).thenReturn(Optional.of(funcionario));
            when(atendimentoRepository.save(any())).thenAnswer(inv -> {
                Atendimento a = inv.getArgument(0);
                a.setId(UUID.randomUUID());
                a.setDataCadastro(LocalDateTime.now());
                a.setDataEntrada(LocalDateTime.now());
                return a;
            });

            AtendimentoDTO resultado = service.cadastrarAtendimento(dto(StatusAtendimento.AGUARDANDO));

            assertThat(resultado.getDataConclusao()).isNull();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarAtendimentoPorId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarAtendimentoPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar DTO quando atendimento existe")
        void deveRetornarDtoQuandoExiste() {
            UUID id = UUID.randomUUID();
            Atendimento a = atendimentoSalvo(StatusAtendimento.AGUARDANDO);
            a.setId(id);
            when(atendimentoRepository.findById(id)).thenReturn(Optional.of(a));

            AtendimentoDTO resultado = service.buscarAtendimentoPorId(id);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("deve lançar AtendimentoNaoEncontrado quando ID não existe")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            UUID id = UUID.randomUUID();
            when(atendimentoRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarAtendimentoPorId(id))
                    .isInstanceOf(AtendimentoNaoEncontrado.class)
                    .hasMessageContaining("Atendimento não encontrado");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarAtendimentos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarTodosAtendimentos")
    class ListarTodos {

        @Test
        @DisplayName("deve retornar lista vazia quando não há atendimentos")
        void deveRetornarListaVazia() {
            when(atendimentoRepository.findAll()).thenReturn(List.of());

            List<AtendimentoDTO> resultado = service.listarTodosAtendimentos();

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve retornar todos os atendimentos mapeados para DTO")
        void deveRetornarTodosAtendimentos() {
            when(atendimentoRepository.findAll()).thenReturn(List.of(
                    atendimentoSalvo(StatusAtendimento.AGUARDANDO),
                    atendimentoSalvo(StatusAtendimento.ANDAMENTO)
            ));

            List<AtendimentoDTO> resultado = service.listarTodosAtendimentos();

            assertThat(resultado).hasSize(2);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarAtendimentosConcluidos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarAtendimentosConcluidos")
    class ListarConcluidos {

        @Test
        @DisplayName("deve retornar apenas atendimentos com status CONCLUIDO")
        void deveRetornarSomenteAtendimentosConcluidos() {
            Atendimento concluido = atendimentoSalvo(StatusAtendimento.CONCLUIDO);
            concluido.setDataConclusao(LocalDateTime.now());
            when(atendimentoRepository.findByStatusConcluido()).thenReturn(List.of(concluido));

            List<AtendimentoDTO> resultado = service.listarAtendimentosConcluidos();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há atendimentos concluídos")
        void deveRetornarListaVaziaQuandoNaoHaConcluidos() {
            when(atendimentoRepository.findByStatusConcluido()).thenReturn(List.of());

            List<AtendimentoDTO> resultado = service.listarAtendimentosConcluidos();

            assertThat(resultado).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarAtendimentosPorClienteID
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarAtendimentosPorClienteID")
    class ListarPorCliente {

        @Test
        @DisplayName("deve retornar atendimentos do cliente quando cliente existe")
        void deveRetornarAtendimentosDoCliente() {
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(atendimentoRepository.findByClienteId(cliente.getId())).thenReturn(
                    List.of(atendimentoSalvo(StatusAtendimento.ANDAMENTO))
            );

            List<AtendimentoDTO> resultado = service.listarAtendimentosPorClienteID(cliente.getId());

            assertThat(resultado).hasSize(1);
            verify(atendimentoRepository).findByClienteId(cliente.getId());
        }

        @Test
        @DisplayName("deve lançar ClienteNaoEncontradoException quando cliente não existe")
        void deveLancarExcecaoClienteNaoEncontrado() {
            when(clienteRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listarAtendimentosPorClienteID(UUID.randomUUID()))
                    .isInstanceOf(ClienteNaoEncontradoException.class);
            verifyNoInteractions(atendimentoRepository);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // deletarAtendimentoPorId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deletarAtendimentoPorId")
    class Deletar {

        @Test
        @DisplayName("deve deletar atendimento quando ID existe")
        void deveDeletarQuandoExiste() {
            Atendimento a = atendimentoSalvo(StatusAtendimento.AGUARDANDO);
            when(atendimentoRepository.findById(a.getId())).thenReturn(Optional.of(a));

            service.deletarAtendimentoPorId(a.getId());

            verify(atendimentoRepository).delete(a);
        }

        @Test
        @DisplayName("deve lançar AtendimentoNaoEncontrado ao deletar ID inexistente")
        void deveLancarExcecaoAoDeletarIdInexistente() {
            when(atendimentoRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletarAtendimentoPorId(UUID.randomUUID()))
                    .isInstanceOf(AtendimentoNaoEncontrado.class);
            verify(atendimentoRepository, never()).delete(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // atualizarAtendimento
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("atualizarAtendimento")
    class AtualizarAtendimento {

        @Test
        @DisplayName("deve atualizar status e preencher dataConclusao ao concluir")
        void deveAtualizarEPreencherDataConclusaoAoConcluir() {
            Atendimento existente = atendimentoSalvo(StatusAtendimento.ANDAMENTO);
            when(atendimentoRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
            when(funcionarioRepository.findById(funcionario.getId())).thenReturn(Optional.of(funcionario));
            when(atendimentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AtendimentoDTO resultado = service.atualizarAtendimento(existente.getId(), dto(StatusAtendimento.CONCLUIDO));

            assertThat(resultado.getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);
            assertThat(resultado.getDataConclusao()).isNotNull();
        }

        @Test
        @DisplayName("deve lançar RuntimeException ao atualizar atendimento inexistente")
        void deveLancarExcecaoAoAtualizarAtendimentoInexistente() {
            when(atendimentoRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarAtendimento(UUID.randomUUID(), dto(StatusAtendimento.ANDAMENTO)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Atendimento não encontrado");
        }

        @Test
        @DisplayName("deve atualizar status de AGUARDANDO para ANDAMENTO sem preencher dataConclusao")
        void deveAtualizarStatusSemPreencherDataConclusao() {
            Atendimento existente = atendimentoSalvo(StatusAtendimento.AGUARDANDO);
            when(atendimentoRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculo));
            when(funcionarioRepository.findById(funcionario.getId())).thenReturn(Optional.of(funcionario));
            when(atendimentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AtendimentoDTO resultado = service.atualizarAtendimento(existente.getId(), dto(StatusAtendimento.ANDAMENTO));

            assertThat(resultado.getStatus()).isEqualTo(StatusAtendimento.ANDAMENTO);
            assertThat(resultado.getDataConclusao()).isNull();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ordenarAtendimentosporDataEntrada
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ordenarAtendimentosporDataEntrada")
    class OrdenarPorDataEntrada {

        @Test
        @DisplayName("deve retornar atendimentos em ordem decrescente por dataEntrada")
        void deveRetornarOrdemDecrescente() {
            Atendimento a1 = atendimentoSalvo(StatusAtendimento.CONCLUIDO);
            a1.setDataEntrada(LocalDateTime.now().minusDays(2));
            Atendimento a2 = atendimentoSalvo(StatusAtendimento.ANDAMENTO);
            a2.setDataEntrada(LocalDateTime.now());

            // repository já retorna na ordem correta (query com ORDER BY)
            when(atendimentoRepository.findAllOrderByDataEntradaDesc()).thenReturn(List.of(a2, a1));

            List<AtendimentoDTO> resultado = service.ordenarAtendimentosporDataEntrada();

            assertThat(resultado).hasSize(2);
            // primeiro elemento deve ter dataEntrada mais recente
            assertThat(resultado.get(0).getDataEntrada())
                    .isAfterOrEqualTo(resultado.get(1).getDataEntrada());
        }
    }
}
