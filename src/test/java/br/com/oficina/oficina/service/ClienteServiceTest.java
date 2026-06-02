package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.dto.cliente.ClienteListaDTO;
import br.com.oficina.oficina.exception.CepNaoEncontradoException;
import br.com.oficina.oficina.exception.ClienteComVeiculosException;
import br.com.oficina.oficina.exception.ClienteNaoEncontradoException;
import br.com.oficina.oficina.exception.RecursoJaCadastradoException;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Endereco;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService — testes unitários")
class ClienteServiceTest {

    @Mock ClienteRepository clienteRepository;
    @Mock VeiculoRepository veiculoRepository;
    @Mock ViaCepService     viaCepService;

    @InjectMocks
    ClienteService service;

    // ── fixtures ──────────────────────────────────────────────────────────────

    private CadastrarClienteDTO dtoValido;
    private Endereco             enderecoMock;
    private Cliente              clienteSalvo;

    @BeforeEach
    void setUp() {
        enderecoMock = new Endereco();
        enderecoMock.setId(1L);
        enderecoMock.setCep("01001000");
        enderecoMock.setLogradouro("Praça da Sé");
        enderecoMock.setNumero("10");
        enderecoMock.setBairro("Sé");
        enderecoMock.setLocalidade("São Paulo");
        enderecoMock.setUf("SP");
        enderecoMock.setComplemento("Apto 2");

        dtoValido = new CadastrarClienteDTO();
        dtoValido.setNomeCompleto("João da Silva");
        dtoValido.setCpfCNPJ("52998224725");
        dtoValido.setTelefone("11987654321");
        dtoValido.setEmail("joao@email.com");
        dtoValido.setCep("01001000");
        dtoValido.setNumero("10");
        dtoValido.setComplemento("Apto 2");

        clienteSalvo = new Cliente();
        clienteSalvo.setId(UUID.randomUUID());
        clienteSalvo.setNomeCompleto("João da Silva");
        clienteSalvo.setCpfCNPJ("52998224725");
        clienteSalvo.setTelefone("11987654321");
        clienteSalvo.setEmail("joao@email.com");
        clienteSalvo.setEndereco(enderecoMock);
        clienteSalvo.setDataCadastro(LocalDateTime.now());
        clienteSalvo.setVeiculos(new ArrayList<>());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // cadastrarCliente
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cadastrarCliente")
    class CadastrarCliente {

        @Test
        @DisplayName("deve cadastrar cliente com sucesso quando dados são válidos")
        void deveCadastrarComSucesso() {
            when(clienteRepository.existsByCpfCNPJ("52998224725")).thenReturn(false);
            when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(false);
            when(viaCepService.buscarEConstruirEndereco("01001000", "10", "Apto 2"))
                    .thenReturn(enderecoMock);
            when(clienteRepository.save(any())).thenReturn(clienteSalvo);

            Cliente resultado = service.cadastrarCliente(dtoValido);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNomeCompleto()).isEqualTo("João da Silva");
            assertThat(resultado.getCpfCNPJ()).isEqualTo("52998224725");
            verify(clienteRepository).save(any(Cliente.class));
        }

        @Test
        @DisplayName("deve lançar RecursoJaCadastradoException quando CPF/CNPJ já existe")
        void deveLancarExcecaoCpfDuplicado() {
            when(clienteRepository.existsByCpfCNPJ("52998224725")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarCliente(dtoValido))
                    .isInstanceOf(RecursoJaCadastradoException.class)
                    .hasMessageContaining("CPF/CNPJ já cadastrado");
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar RecursoJaCadastradoException quando e-mail já existe")
        void deveLancarExcecaoEmailDuplicado() {
            when(clienteRepository.existsByCpfCNPJ(any())).thenReturn(false);
            when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarCliente(dtoValido))
                    .isInstanceOf(RecursoJaCadastradoException.class)
                    .hasMessageContaining("Email já cadastrado");
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando CEP não é encontrado via ViaCEP")
        void deveLancarExcecaoCepInvalido() {
            when(clienteRepository.existsByCpfCNPJ(any())).thenReturn(false);
            when(clienteRepository.existsByEmail(any())).thenReturn(false);
            when(viaCepService.buscarEConstruirEndereco(any(), any(), any())).thenReturn(null);

            assertThatThrownBy(() -> service.cadastrarCliente(dtoValido))
                    .isInstanceOf(CepNaoEncontradoException.class)
                    .hasMessageContaining("CEP não encontrado");
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve normalizar email para minúsculas ao cadastrar")
        void deveNormalizarEmailParaMinusculas() {
            dtoValido.setEmail("JOAO@EMAIL.COM");
            when(clienteRepository.existsByCpfCNPJ(any())).thenReturn(false);
            when(clienteRepository.existsByEmail("JOAO@EMAIL.COM")).thenReturn(false);
            when(viaCepService.buscarEConstruirEndereco(any(), any(), any())).thenReturn(enderecoMock);
            when(clienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente resultado = service.cadastrarCliente(dtoValido);

            assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarCliente
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarCliente")
    class BuscarCliente {

        @Test
        @DisplayName("deve retornar cliente quando ID existe")
        void deveBuscarPorIdComSucesso() {
            when(clienteRepository.findById(clienteSalvo.getId())).thenReturn(Optional.of(clienteSalvo));

            Cliente resultado = service.buscarClientePorId(clienteSalvo.getId());

            assertThat(resultado.getId()).isEqualTo(clienteSalvo.getId());
        }

        @Test
        @DisplayName("deve lançar ClienteNaoEncontradoException quando ID não existe")
        void deveLancarExcecaoPorIdInexistente() {
            when(clienteRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarClientePorId(UUID.randomUUID()))
                    .isInstanceOf(ClienteNaoEncontradoException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }

        @Test
        @DisplayName("deve retornar cliente quando CPF/CNPJ existe")
        void deveBuscarPorCpfComSucesso() {
            when(clienteRepository.findByCpfCNPJ("52998224725")).thenReturn(Optional.of(clienteSalvo));

            Cliente resultado = service.buscarClientePorCpfCNPJ("52998224725");

            assertThat(resultado.getCpfCNPJ()).isEqualTo("52998224725");
        }

        @Test
        @DisplayName("deve normalizar CPF/CNPJ antes de buscar (remove pontuação)")
        void deveNormalizarCpfAntesDeConsultar() {
            when(clienteRepository.findByCpfCNPJ("52998224725")).thenReturn(Optional.of(clienteSalvo));

            // Envia CPF formatado com pontuação
            Cliente resultado = service.buscarClientePorCpfCNPJ("529.982.247-25");

            assertThat(resultado).isNotNull();
            verify(clienteRepository).findByCpfCNPJ("52998224725");
        }

        @Test
        @DisplayName("deve lançar ClienteNaoEncontradoException quando CPF/CNPJ não existe")
        void deveLancarExcecaoPorCpfInexistente() {
            when(clienteRepository.findByCpfCNPJ(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarClientePorCpfCNPJ("00000000000"))
                    .isInstanceOf(ClienteNaoEncontradoException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarTodosClientes
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarTodosClientes")
    class ListarClientes {

        @Test
        @DisplayName("deve retornar lista de DTOs com quantidadeVeiculos preenchida")
        void deveRetornarListaComQuantidadeVeiculos() {
            when(clienteRepository.findAll()).thenReturn(List.of(clienteSalvo));

            List<ClienteListaDTO> resultado = service.listarTodosClientes();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNomeCompleto()).isEqualTo("João da Silva");
            assertThat(resultado.get(0).getQuantidadeVeiculos()).isZero();
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há clientes")
        void deveRetornarListaVazia() {
            when(clienteRepository.findAll()).thenReturn(List.of());

            List<ClienteListaDTO> resultado = service.listarTodosClientes();

            assertThat(resultado).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // atualizarCliente
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("atualizarCliente")
    class AtualizarCliente {

        @Test
        @DisplayName("deve atualizar cliente com sucesso quando dados e endereco sao validos")
        void deveAtualizarComSucesso() {
            when(clienteRepository.findById(clienteSalvo.getId())).thenReturn(Optional.of(clienteSalvo));

            CadastrarClienteDTO upd = new CadastrarClienteDTO();
            upd.setNomeCompleto("Joao Atualizado");
            upd.setCpfCNPJ("52998224725");
            upd.setTelefone("11988887777");
            upd.setEmail("joao.atualizado@email.com");
            upd.setCep("01001000");
            upd.setNumero("10");
            upd.setComplemento("Apto 2");

            when(clienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente resultado = service.atualizarCliente(clienteSalvo.getId(), upd);

            assertThat(resultado.getNomeCompleto()).isEqualTo("Joao Atualizado");
            assertThat(resultado.getTelefone()).isEqualTo("11988887777");
            // mesmo CEP/numero/complemento → sem chamada ao ViaCEP
            verify(viaCepService, never()).buscarEConstruirEndereco(any(), any(), any());
        }

        @Test
        @DisplayName("deve buscar novo endereco via ViaCEP quando CEP é alterado")
        void deveBuscarNovoEnderecoQuandoCepAlterado() {
            Endereco enderecoOriginal = new Endereco();
            enderecoOriginal.setId(1L);
            enderecoOriginal.setCep("01001000");
            enderecoOriginal.setLogradouro("Praça da Sé");
            enderecoOriginal.setNumero("10");
            enderecoOriginal.setBairro("Sé");
            enderecoOriginal.setLocalidade("São Paulo");
            enderecoOriginal.setUf("SP");
            clienteSalvo.setEndereco(enderecoOriginal);

            when(clienteRepository.findById(clienteSalvo.getId())).thenReturn(Optional.of(clienteSalvo));

            Endereco novoEndereco = new Endereco();
            novoEndereco.setCep("20040002");
            novoEndereco.setLogradouro("Av. Rio Branco");
            novoEndereco.setNumero("100");
            novoEndereco.setBairro("Centro");
            novoEndereco.setLocalidade("Rio de Janeiro");
            novoEndereco.setUf("RJ");

            CadastrarClienteDTO upd = new CadastrarClienteDTO();
            upd.setNomeCompleto("Joao Atualizado");
            upd.setCpfCNPJ("52998224725");
            upd.setTelefone("11988887777");
            upd.setEmail("joao.atualizado@email.com");
            upd.setCep("20040002");
            upd.setNumero("100");
            upd.setComplemento("Sala 1");

            when(viaCepService.buscarEConstruirEndereco("20040002", "100", "Sala 1"))
                    .thenReturn(novoEndereco);
            when(clienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente resultado = service.atualizarCliente(clienteSalvo.getId(), upd);

            assertThat(resultado.getEndereco().getCep()).isEqualTo("20040002");
            verify(viaCepService).buscarEConstruirEndereco("20040002", "100", "Sala 1");
        }

        @Test
        @DisplayName("deve lancar ClienteNaoEncontradoException ao atualizar ID inexistente")
        void deveLancarExcecaoClienteNaoEncontrado() {
            when(clienteRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarCliente(UUID.randomUUID(), dtoValido))
                    .isInstanceOf(ClienteNaoEncontradoException.class);
            verify(clienteRepository, never()).save(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // deletarCliente — regra de negócio: não deletar se tiver veículos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deletarClientePorId")
    class DeletarCliente {

        @Test
        @DisplayName("deve deletar cliente quando não há veículos associados")
        void deveDeletarQuandoSemVeiculos() {
            when(clienteRepository.findById(clienteSalvo.getId())).thenReturn(Optional.of(clienteSalvo));
            when(veiculoRepository.countByClienteId(clienteSalvo.getId())).thenReturn(0L);

            service.deletarClientePorId(clienteSalvo.getId());

            verify(clienteRepository).delete(clienteSalvo);
        }

        @Test
        @DisplayName("[REGRA] deve lançar ClienteComVeiculosException ao tentar deletar cliente com veículos")
        void deveLancarExcecaoClienteComVeiculos() {
            when(clienteRepository.findById(clienteSalvo.getId())).thenReturn(Optional.of(clienteSalvo));
            when(veiculoRepository.countByClienteId(clienteSalvo.getId())).thenReturn(3L);

            assertThatThrownBy(() -> service.deletarClientePorId(clienteSalvo.getId()))
                    .isInstanceOf(ClienteComVeiculosException.class)
                    .hasMessageContaining("3 veículo(s)");
            verify(clienteRepository, never()).delete(any());
        }

        @Test
        @DisplayName("[REGRA] mensagem de erro deve informar a quantidade exata de veículos vinculados")
        void mensagemDeErroDeveConterQuantidadeExataDeVeiculos() {
            when(clienteRepository.findById(clienteSalvo.getId())).thenReturn(Optional.of(clienteSalvo));
            when(veiculoRepository.countByClienteId(clienteSalvo.getId())).thenReturn(5L);

            assertThatThrownBy(() -> service.deletarClientePorId(clienteSalvo.getId()))
                    .isInstanceOf(ClienteComVeiculosException.class)
                    .hasMessageContaining("5 veículo(s)");
        }

        @Test
        @DisplayName("deve lançar ClienteNaoEncontradoException ao deletar ID inexistente")
        void deveLancarExcecaoClienteNaoEncontrado() {
            when(clienteRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletarClientePorId(UUID.randomUUID()))
                    .isInstanceOf(ClienteNaoEncontradoException.class);
            verify(clienteRepository, never()).delete(any());
        }
    }
}
