package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.exception.FuncionarioComAtendimentosException;
import br.com.oficina.oficina.exception.FuncionarioNaoEncontrado;
import br.com.oficina.oficina.exception.RecursoJaCadastradoException;
import br.com.oficina.oficina.mapper.FuncionarioMapper;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.repository.AtendimentoRepository;
import br.com.oficina.oficina.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FuncionarioService — testes unitários")
class FuncionarioServiceTest {

    @Mock FuncionarioRepository    funcionarioRepository;
    @Mock PasswordEncoder           passwordEncoder;
    @Mock FuncionarioMapper         funcionarioMapper;
    @Mock AtendimentoRepository     atendimentoRepository;

    @InjectMocks
    FuncionarioService service;

    // ── fixtures ──────────────────────────────────────────────────────────────

    private Funcionario funcionario;
    private final UUID   FUNC_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario();
        funcionario.setId(FUNC_ID);
        funcionario.setNome("Carlos Admin");
        funcionario.setCpfCNPJ("52998224725");
        funcionario.setUsuario("carlos.adm");
        funcionario.setSenhaHash("$2a$10$encodedhash");
        funcionario.setCargo("ADMIN");
        funcionario.setTelefone("11988888888");
        funcionario.setEmail("carlos@oficina.com");
    }

    private CadastrarFuncionarioDTO dtoValido() {
        CadastrarFuncionarioDTO d = new CadastrarFuncionarioDTO();
        d.setNome("Carlos Admin");
        d.setCpfCNPJ("52998224725");
        d.setUsuario("carlos.adm");
        d.setSenha("senha123");
        d.setCargo("ADMIN");
        d.setTelefone("11988888888");
        d.setEmail("carlos@oficina.com");
        return d;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // cadastrarFuncionario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cadastrarFuncionario")
    class CadastrarFuncionario {

        @Test
        @DisplayName("deve cadastrar funcionario com sucesso quando dados sao validos")
        void deveCadastrarComSucesso() {
            when(funcionarioRepository.existsByCpfCNPJ("52998224725")).thenReturn(false);
            when(funcionarioRepository.existsByUsuario("carlos.adm")).thenReturn(false);
            when(funcionarioRepository.existsByEmail("carlos@oficina.com")).thenReturn(false);
            when(funcionarioMapper.toEntity(any())).thenReturn(funcionario);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedhash");
            when(funcionarioRepository.save(any())).thenReturn(funcionario);

            Funcionario resultado = service.cadastrarFuncionario(dtoValido());

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Carlos Admin");
            assertThat(resultado.getUsuario()).isEqualTo("carlos.adm");
            verify(funcionarioRepository).save(any(Funcionario.class));
        }

        @Test
        @DisplayName("deve lancar RecursoJaCadastradoException quando CPF ja existe")
        void deveLancarExcecaoCpfDuplicado() {
            when(funcionarioRepository.existsByCpfCNPJ("52998224725")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarFuncionario(dtoValido()))
                    .isInstanceOf(RecursoJaCadastradoException.class)
                    .hasMessageContaining("CPF/CNPJ já cadastrado");
            verify(funcionarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lancar RecursoJaCadastradoException quando usuario ja existe")
        void deveLancarExcecaoUsuarioDuplicado() {
            when(funcionarioRepository.existsByCpfCNPJ(any())).thenReturn(false);
            when(funcionarioRepository.existsByUsuario("carlos.adm")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarFuncionario(dtoValido()))
                    .isInstanceOf(RecursoJaCadastradoException.class)
                    .hasMessageContaining("Usuário já cadastrado");
            verify(funcionarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lancar RecursoJaCadastradoException quando email ja existe")
        void deveLancarExcecaoEmailDuplicado() {
            when(funcionarioRepository.existsByCpfCNPJ(any())).thenReturn(false);
            when(funcionarioRepository.existsByUsuario(any())).thenReturn(false);
            when(funcionarioRepository.existsByEmail("carlos@oficina.com")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarFuncionario(dtoValido()))
                    .isInstanceOf(RecursoJaCadastradoException.class)
                    .hasMessageContaining("Email já cadastrado");
            verify(funcionarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lancar IllegalArgumentException quando DTO eh nulo")
        void deveLancarExcecaoParaDtoNulo() {
            assertThatThrownBy(() -> service.cadastrarFuncionario(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Dados do funcionário são obrigatórios");
            verifyNoInteractions(funcionarioRepository);
        }

        @Test
        @DisplayName("deve codificar a senha antes de salvar")
        void deveCodificarSenhaAntesDeSalvar() {
            when(funcionarioRepository.existsByCpfCNPJ(any())).thenReturn(false);
            when(funcionarioRepository.existsByUsuario(any())).thenReturn(false);
            when(funcionarioRepository.existsByEmail(any())).thenReturn(false);
            when(funcionarioMapper.toEntity(any())).thenReturn(funcionario);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedhash");
            when(funcionarioRepository.save(any())).thenReturn(funcionario);

            service.cadastrarFuncionario(dtoValido());

            verify(passwordEncoder).encode("senha123");
            assertThat(funcionario.getSenhaHash()).isEqualTo("$2a$10$encodedhash");
        }

        @Test
        @DisplayName("deve delegar ao mapper apos validacoes passarem")
        void deveDelegarAoMapperAposValidacoes() {
            when(funcionarioRepository.existsByCpfCNPJ(anyString())).thenReturn(false);
            when(funcionarioRepository.existsByUsuario(anyString())).thenReturn(false);
            when(funcionarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(funcionarioMapper.toEntity(any())).thenReturn(funcionario);
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(funcionarioRepository.save(any())).thenReturn(funcionario);

            service.cadastrarFuncionario(dtoValido());

            verify(funcionarioMapper).toEntity(any(CadastrarFuncionarioDTO.class));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarPorId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar DTO quando funcionario existe")
        void deveRetornarDtoQuandoExiste() {
            when(funcionarioRepository.findById(FUNC_ID)).thenReturn(Optional.of(funcionario));

            FuncionarioDTO resultado = service.buscarPorId(FUNC_ID);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(FUNC_ID);
            assertThat(resultado.getNome()).isEqualTo("Carlos Admin");
        }

        @Test
        @DisplayName("deve lancar FuncionarioNaoEncontrado quando ID nao existe")
        void deveLancarExcecaoQuandoInexistente() {
            when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(UUID.randomUUID()))
                    .isInstanceOf(FuncionarioNaoEncontrado.class)
                    .hasMessageContaining("Funcionário não encontrado");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // buscarPorUsuario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("buscarPorUsuario")
    class BuscarPorUsuario {

        @Test
        @DisplayName("deve retornar Optional com funcionario quando usuario existe")
        void deveRetornarFuncionarioQuandoExiste() {
            when(funcionarioRepository.findByUsuario("carlos.adm")).thenReturn(Optional.of(funcionario));

            Optional<Funcionario> resultado = service.buscarPorUsuario("carlos.adm");

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getUsuario()).isEqualTo("carlos.adm");
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando usuario nao existe")
        void deveRetornarVazioQuandoInexistente() {
            when(funcionarioRepository.findByUsuario("inexistente")).thenReturn(Optional.empty());

            Optional<Funcionario> resultado = service.buscarPorUsuario("inexistente");

            assertThat(resultado).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // autenticar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("autenticar")
    class Autenticar {

        @Test
        @DisplayName("deve retornar true quando usuario e senha estao corretos")
        void deveAutenticarComSucesso() {
            when(funcionarioRepository.findByUsuario("carlos.adm")).thenReturn(Optional.of(funcionario));
            when(passwordEncoder.matches("senha123", "$2a$10$encodedhash")).thenReturn(true);

            boolean resultado = service.autenticar("carlos.adm", "senha123");

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando senha esta incorreta")
        void deveRetornarFalseParaSenhaIncorreta() {
            when(funcionarioRepository.findByUsuario("carlos.adm")).thenReturn(Optional.of(funcionario));
            when(passwordEncoder.matches("senha-errada", "$2a$10$encodedhash")).thenReturn(false);

            boolean resultado = service.autenticar("carlos.adm", "senha-errada");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando usuario nao existe")
        void deveRetornarFalseParaUsuarioInexistente() {
            when(funcionarioRepository.findByUsuario("inexistente")).thenReturn(Optional.empty());

            boolean resultado = service.autenticar("inexistente", "senha123");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para usuario ou senha em branco")
        void deveRetornarFalseParaCredenciaisVazias() {
            assertThat(service.autenticar("", "senha123")).isFalse();
            assertThat(service.autenticar("carlos.adm", "")).isFalse();
            assertThat(service.autenticar(null, "senha123")).isFalse();
            assertThat(service.autenticar("carlos.adm", null)).isFalse();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarTodosFuncionarios
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarTodosFuncionarios")
    class ListarTodos {

        @Test
        @DisplayName("deve retornar lista de funcionarios")
        void deveRetornarLista() {
            when(funcionarioRepository.findAll()).thenReturn(List.of(funcionario));

            List<Funcionario> resultado = service.listarTodosFuncionarios();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNome()).isEqualTo("Carlos Admin");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando nao ha funcionarios")
        void deveRetornarListaVazia() {
            when(funcionarioRepository.findAll()).thenReturn(List.of());

            List<Funcionario> resultado = service.listarTodosFuncionarios();

            assertThat(resultado).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // deletarFuncionarioPorId
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deletarFuncionarioPorId")
    class DeletarFuncionario {

        @Test
        @DisplayName("deve deletar funcionario quando ID existe")
        void deveDeletarComSucesso() {
            when(funcionarioRepository.findById(FUNC_ID)).thenReturn(Optional.of(funcionario));

            service.deletarFuncionarioPorId(FUNC_ID);

            verify(funcionarioRepository).deleteById(FUNC_ID);
        }

        @Test
        @DisplayName("deve lancar FuncionarioNaoEncontrado ao deletar ID inexistente")
        void deveLancarExcecaoParaIdInexistente() {
            when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletarFuncionarioPorId(UUID.randomUUID()))
                    .isInstanceOf(FuncionarioNaoEncontrado.class);
            verify(funcionarioRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("[REGRA] deve lancar FuncionarioComAtendimentosException ao deletar funcionario com atendimentos")
        void deveLancarExcecaoFuncionarioComAtendimentos() {
            when(funcionarioRepository.findById(FUNC_ID)).thenReturn(Optional.of(funcionario));
            when(atendimentoRepository.countByFuncionarioId(FUNC_ID)).thenReturn(3L);

            assertThatThrownBy(() -> service.deletarFuncionarioPorId(FUNC_ID))
                    .isInstanceOf(FuncionarioComAtendimentosException.class)
                    .hasMessageContaining("3 atendimento(s)");
            verify(funcionarioRepository, never()).deleteById(any());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // atualizarFuncionario
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("atualizarFuncionario")
    class AtualizarFuncionario {

        @Test
        @DisplayName("deve atualizar dados do funcionario com sucesso")
        void deveAtualizarComSucesso() {
            when(funcionarioRepository.findById(FUNC_ID)).thenReturn(Optional.of(funcionario));
            when(funcionarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CadastrarFuncionarioDTO upd = dtoValido();
            upd.setNome("Carlos Atualizado");
            upd.setCargo("MECANICO");
            upd.setTelefone("11999999999");
            upd.setEmail("carlos.novo@oficina.com");

            Funcionario resultado = service.atualizarFuncionario(FUNC_ID, upd);

            assertThat(resultado.getNome()).isEqualTo("Carlos Atualizado");
            assertThat(resultado.getCargo()).isEqualTo("MECANICO");
            assertThat(resultado.getTelefone()).isEqualTo("11999999999");
            assertThat(resultado.getEmail()).isEqualTo("carlos.novo@oficina.com");
        }

        @Test
        @DisplayName("deve atualizar senha apenas quando fornecida")
        void deveAtualizarSenhaQuandoFornecida() {
            when(funcionarioRepository.findById(FUNC_ID)).thenReturn(Optional.of(funcionario));
            when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$novohash");
            when(funcionarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CadastrarFuncionarioDTO upd = dtoValido();
            upd.setSenha("novaSenha123");

            Funcionario resultado = service.atualizarFuncionario(FUNC_ID, upd);

            verify(passwordEncoder).encode("novaSenha123");
            assertThat(resultado.getSenhaHash()).isEqualTo("$2a$10$novohash");
        }

        @Test
        @DisplayName("nao deve alterar senha quando campo senha estiver em branco")
        void naoDeveAlterarSenhaQuandoEmBranco() {
            funcionario.setSenhaHash("$2a$10$senhaoriginal");
            when(funcionarioRepository.findById(FUNC_ID)).thenReturn(Optional.of(funcionario));
            when(funcionarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CadastrarFuncionarioDTO upd = dtoValido();
            upd.setSenha("");
            upd.setSenha(null);

            Funcionario resultado = service.atualizarFuncionario(FUNC_ID, upd);

            verify(passwordEncoder, never()).encode(any());
            assertThat(resultado.getSenhaHash()).isEqualTo("$2a$10$senhaoriginal");
        }

        @Test
        @DisplayName("deve lancar FuncionarioNaoEncontrado ao atualizar ID inexistente")
        void deveLancarExcecaoFuncionarioNaoEncontrado() {
            when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarFuncionario(UUID.randomUUID(), dtoValido()))
                    .isInstanceOf(FuncionarioNaoEncontrado.class);
            verify(funcionarioRepository, never()).save(any());
        }
    }
}
