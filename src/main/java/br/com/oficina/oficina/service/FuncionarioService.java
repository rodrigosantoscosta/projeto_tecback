package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.atendimento.AtendimentoDTO;
import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.exception.AtendimentoNaoEncontrado;
import br.com.oficina.oficina.exception.FuncionarioNaoEncontrado;
import br.com.oficina.oficina.exception.RecursoJaCadastradoException;
import br.com.oficina.oficina.mapper.FuncionarioMapper;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.repository.FuncionarioRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FuncionarioMapper funcionarioMapper;

    public List<Funcionario> listarTodosFuncionarios() {
        log.info("Listando todos os funcionários");
        List<Funcionario> funcionarios = funcionarioRepository.findAll();
        log.debug("total de funcionários encontrados: {}", funcionarios.size());

        return funcionarioRepository.findAll();
    }

    public FuncionarioDTO buscarPorId(UUID id) {
        log.debug("Buscando cliente por ID: {}", id);
        return funcionarioRepository.findById(id)
                .map(FuncionarioDTO::new)
                .orElseThrow(() -> new AtendimentoNaoEncontrado("Atendimento não encontrado"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Transactional
    public Funcionario cadastrarFuncionario(CadastrarFuncionarioDTO funcionarioDTO) {
        log.info("Iniciando cadastro de funcionário: {}", funcionarioDTO.getNome());
        if (funcionarioDTO == null) {
            log.error("Dados do funcionário não encontrado: {}", funcionarioDTO);
            throw new IllegalArgumentException("Dados do funcionário são obrigatórios");
        }

        //Valida unicidade
        if (funcionarioRepository.existsByCpfCNPJ(funcionarioDTO.getCpfCNPJ())) {
            log.error("CPF/CNPJ já cadastrado: {}", funcionarioDTO.getCpfCNPJ());
            throw new RecursoJaCadastradoException("CPF/CNPJ já cadastrado no sistema");
        }

        if (funcionarioRepository.existsByUsuario(funcionarioDTO.getUsuario())) {
            log.error("Usuario já cadastrado: {}", funcionarioDTO.getUsuario());
            throw new RecursoJaCadastradoException("Usuário já cadastrado no sistema");
        }

        if (funcionarioRepository.existsByEmail(funcionarioDTO.getEmail())) {
            log.error("Email já cadastrado: {}", funcionarioDTO.getEmail());
            throw new RecursoJaCadastradoException("Email já cadastrado no sistema");
        }

        // Normalizar CPF/CNPJ (remover caracteres não numéricos)
        String cpfNormalizado = funcionarioDTO.getCpfCNPJ().replaceAll("\\D", "");
        if (!StringUtils.hasText(cpfNormalizado)) {
            throw new IllegalArgumentException("CPF/CNPJ é obrigatório");
        }
        funcionarioDTO.setCpfCNPJ(cpfNormalizado);

        // Usar o mapper para criar a entidade
        Funcionario funcionario = funcionarioMapper.toEntity(funcionarioDTO);

        // Configurar campos adicionais que não são mapeados automaticamente
        funcionario.setSenhaHash(passwordEncoder.encode(funcionarioDTO.getSenha()));

        return funcionarioRepository.save(funcionario);
    }



    public boolean autenticar(String usuario, String senha) {
        if (!StringUtils.hasText(usuario) || !StringUtils.hasText(senha)) {
            return false;
        }

        return funcionarioRepository.findByUsuario(usuario)
                .map(funcionario -> passwordEncoder.matches(senha, funcionario.getSenhaHash()))
                .orElse(false);
    }

    @Transactional
    public void deletarFuncionarioPorId(UUID id) {
        log.info("Deletando funcionário: {}", id);

        // Verifica se o funcionario existe
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Funcionário não encontrado para deleção: {}", id);
                    return new FuncionarioNaoEncontrado(
                            "Funcionário não encontrado com ID: " + id
                    );
                });
        funcionarioRepository.deleteById(id);
        log.info("Funcionário deletado com sucesso: {}", id);
    }


    // Added: buscar por usuario
    public Optional<Funcionario> buscarPorUsuario(String usuario) {
        return funcionarioRepository.findByUsuario(usuario);
    }

    @Transactional
    public Funcionario atualizarFuncionario(UUID id, CadastrarFuncionarioDTO funcionarioDTO) {
        log.info("Atualiza funcionário com ID: {}", id);

        Funcionario funcionarioExistente = funcionarioRepository.findById(id)
                .orElseThrow(() ->{
                    log.error("Funcionário não encontrado para atualização: {}", id);
                    return new FuncionarioNaoEncontrado(
                            "Funcionário não encontrado com ID: "+id
                    );
                });


        //Atualiza os dados básicos do funcionário
        funcionarioExistente.setNome(funcionarioDTO.getNome().trim());
        funcionarioExistente.setCpfCNPJ(funcionarioDTO.getCpfCNPJ().replaceAll("\\D", ""));
        funcionarioExistente.setEmail(funcionarioDTO.getEmail().trim().toLowerCase());

        Funcionario funcionarioAtualizado = funcionarioRepository.save(funcionarioExistente);
        log.info("funcionario atualizado com sucesso - ID: {}", id);

        return funcionarioAtualizado;
    }
}
