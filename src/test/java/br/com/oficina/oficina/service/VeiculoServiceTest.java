package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.dto.veiculo.VeiculoDTO;
import br.com.oficina.oficina.exception.ClienteNaoEncontradoException;
import br.com.oficina.oficina.exception.RecursoJaCadastradoException;
import br.com.oficina.oficina.exception.VeiculoNaoEncontradoException;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.AtendimentoRepository;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoService — testes unitários")
class VeiculoServiceTest {

    @Mock VeiculoRepository veiculoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock AtendimentoRepository atendimentoRepository;

    @InjectMocks
    VeiculoService service;

    // ── fixtures ──────────────────────────────────────────────────────────────

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNomeCompleto("Carlos Souza");
    }

    private CadastrarVeiculoDTO dto(String placa, Double quilometragem) {
        CadastrarVeiculoDTO d = new CadastrarVeiculoDTO();
        d.setPlaca(placa);
        d.setModelo("Civic");
        d.setMarca("Honda");
        d.setAno(2022);
        d.setCor("Prata");
        d.setQuilometragem(quilometragem);
        d.setClienteId(cliente.getId());
        return d;
    }

    private Veiculo veiculoExistente(String placa, Double quilometragem) {
        Veiculo v = new Veiculo();
        v.setId(UUID.randomUUID());
        v.setPlaca(placa);
        v.setModelo("Civic");
        v.setMarca("Honda");
        v.setAno(2022);
        v.setCor("Prata");
        v.setQuilometragem(quilometragem);
        v.setCliente(cliente);
        return v;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // cadastrarVeiculo
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cadastrarVeiculo")
    class CadastrarVeiculo {

        @Test
        @DisplayName("deve cadastrar veículo com sucesso quando placa e cliente são válidos")
        void deveCadastrarComSucesso() {
            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.save(any())).thenAnswer(inv -> {
                Veiculo v = inv.getArgument(0);
                v.setId(UUID.randomUUID());
                return v;
            });

            VeiculoDTO resultado = service.cadastrarVeiculo(dto("ABC1D23", 10000.0));

            assertThat(resultado).isNotNull();
            assertThat(resultado.getPlaca()).isEqualTo("ABC1D23");
            assertThat(resultado.getClienteId()).isEqualTo(cliente.getId());
            verify(veiculoRepository).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("deve normalizar placa para maiúsculas e sem espaços")
        void deveNormalizarPlaca() {
            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VeiculoDTO resultado = service.cadastrarVeiculo(dto("abc1d23", 0.0));

            assertThat(resultado.getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("deve lançar RecursoJaCadastradoException quando placa já existe")
        void deveLancarExcecaoPlacaDuplicada() {
            when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarVeiculo(dto("ABC1D23", 0.0)))
                    .isInstanceOf(RecursoJaCadastradoException.class)
                    .hasMessageContaining("Placa já cadastrada");
            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ClienteNaoEncontradoException quando cliente não existe")
        void deveLancarExcecaoClienteNaoEncontrado() {
            when(veiculoRepository.existsByPlaca(any())).thenReturn(false);
            when(clienteRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cadastrarVeiculo(dto("ABC1D23", 0.0)))
                    .isInstanceOf(ClienteNaoEncontradoException.class)
                    .hasMessageContaining("Cliente não encontrado");
            verify(veiculoRepository, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarVeiculoPorId / buscarVeiculoPorPlaca
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarVeiculo")
    class BuscarVeiculo {

        @Test
        @DisplayName("deve retornar veículo quando ID existe")
        void deveBuscarPorIdComSucesso() {
            Veiculo v = veiculoExistente("ABC1D23", 5000.0);
            when(veiculoRepository.findById(v.getId())).thenReturn(Optional.of(v));

            VeiculoDTO resultado = service.buscarVeiculoPorId(v.getId());

            assertThat(resultado.getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("deve lançar VeiculoNaoEncontradoException quando ID não existe")
        void deveLancarExcecaoPorIdInexistente() {
            when(veiculoRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarVeiculoPorId(UUID.randomUUID()))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("deve retornar veículo quando placa existe")
        void deveBuscarPorPlacaComSucesso() {
            Veiculo v = veiculoExistente("ABC1D23", 5000.0);
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(v));

            VeiculoDTO resultado = service.buscarVeiculoPorPlaca("ABC1D23");

            assertThat(resultado.getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("deve lançar VeiculoNaoEncontradoException quando placa não existe")
        void deveLancarExcecaoPorPlacaInexistente() {
            when(veiculoRepository.findByPlaca(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarVeiculoPorPlaca("ZZZ0000"))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // atualizarVeiculo
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("atualizarVeiculo")
    class AtualizarVeiculo {

        @Test
        @DisplayName("deve atualizar veículo com sucesso quando dados são válidos")
        void deveAtualizarComSucesso() {
            Veiculo existente = veiculoExistente("ABC1D23", 10000.0);
            when(veiculoRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
            // mesma placa → service não chama existsByPlaca, então não stubamos
            when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
            when(veiculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CadastrarVeiculoDTO update = dto("ABC1D23", 20000.0);
            update.setModelo("Civic EX");
            VeiculoDTO resultado = service.atualizarVeiculo(existente.getId(), update);

            assertThat(resultado.getModelo()).isEqualTo("Civic EX");
            assertThat(resultado.getQuilometragem()).isEqualTo(20000.0);
        }

        @Test
        @DisplayName("deve lançar RecursoJaCadastradoException ao tentar mudar para placa de outro veículo")
        void deveLancarExcecaoPlacaDeOutroVeiculo() {
            Veiculo existente = veiculoExistente("ABC1D23", 10000.0);
            when(veiculoRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
            when(veiculoRepository.existsByPlaca("XYZ9A87")).thenReturn(true);

            CadastrarVeiculoDTO update = dto("XYZ9A87", 10000.0);

            assertThatThrownBy(() -> service.atualizarVeiculo(existente.getId(), update))
                    .isInstanceOf(RecursoJaCadastradoException.class)
                    .hasMessageContaining("Placa já cadastrada para outro veículo");
            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar VeiculoNaoEncontradoException ao atualizar veículo inexistente")
        void deveLancarExcecaoVeiculoNaoEncontrado() {
            when(veiculoRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarVeiculo(UUID.randomUUID(), dto("ABC1D23", 0.0)))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // deletarVeiculo
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deletarVeiculo")
    class DeletarVeiculo {

        @Test
        @DisplayName("deve deletar veículo por ID quando existe")
        void deveDeletarPorIdComSucesso() {
            Veiculo v = veiculoExistente("ABC1D23", 5000.0);
            when(veiculoRepository.findById(v.getId())).thenReturn(Optional.of(v));

            service.deletarVeiculoPorId(v.getId());

            verify(veiculoRepository).delete(v);
        }

        @Test
        @DisplayName("deve lançar VeiculoNaoEncontradoException ao deletar por ID inexistente")
        void deveLancarExcecaoAoDeletarPorIdInexistente() {
            when(veiculoRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletarVeiculoPorId(UUID.randomUUID()))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
            verify(veiculoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deve deletar veículo por placa quando existe")
        void deveDeletarPorPlacaComSucesso() {
            Veiculo v = veiculoExistente("ABC1D23", 5000.0);
            when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(v));

            service.deletarVeiculoPorPlaca("ABC1D23");

            verify(veiculoRepository).delete(v);
        }

        @Test
        @DisplayName("deve lançar VeiculoNaoEncontradoException ao deletar por placa inexistente")
        void deveLancarExcecaoAoDeletarPorPlacaInexistente() {
            when(veiculoRepository.findByPlaca(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletarVeiculoPorPlaca("ZZZ0000"))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
            verify(veiculoRepository, never()).delete(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarVeiculosPorCliente / contarTotalVeiculos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarVeiculosPorCliente")
    class ListarPorCliente {

        @Test
        @DisplayName("deve retornar veículos do cliente")
        void deveRetornarVeiculosDoCliente() {
            when(veiculoRepository.findVeiculoByClienteId(cliente.getId())).thenReturn(
                    List.of(veiculoExistente("ABC1D23", 5000.0), veiculoExistente("XYZ9A87", 1000.0))
            );

            List<VeiculoDTO> resultado = service.listarVeiculosPorCliente(cliente.getId());

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando cliente não tem veículos")
        void deveRetornarListaVaziaQuandoClienteSemVeiculos() {
            when(veiculoRepository.findVeiculoByClienteId(any())).thenReturn(List.of());

            List<VeiculoDTO> resultado = service.listarVeiculosPorCliente(UUID.randomUUID());

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("contarTotalVeiculos")
    class ContarTotal {

        @Test
        @DisplayName("deve retornar total de veículos cadastrados")
        void deveRetornarTotalDeVeiculos() {
            when(veiculoRepository.contarTotalVeiculos()).thenReturn(5L);

            Long total = service.contarTotalVeiculos();

            assertThat(total).isEqualTo(5L);
        }
    }
}
