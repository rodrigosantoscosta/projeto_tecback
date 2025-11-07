package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.funcionario.CriarFuncionarioDTO;
import br.com.oficina.oficina.mapper.FuncionarioMapper;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FuncionarioMapper funcionarioMapper;

    public List<Funcionario> listarTodosFuncionarios() {
        return funcionarioRepository.findAll();
    }

    public java.util.Optional<Funcionario> buscarPorId(UUID id) {
        return funcionarioRepository.findById(id);
    }


    @Transactional
    public Funcionario cadastrarFuncionario(CriarFuncionarioDTO funcionarioDTO) {
        if (funcionarioDTO == null) {
            throw new IllegalArgumentException("Dados do funcionário são obrigatórios");
        }

        // Validar se o CPF/CNPJ está presente (validação principal)
        if (!StringUtils.hasText(funcionarioDTO.getCpfCNPJ())) {
            throw new IllegalArgumentException("CPF/CNPJ é obrigatório");
        }

        // Normalização de dados
        funcionarioDTO.setNome(funcionarioDTO.getNome().trim());
        funcionarioDTO.setCargo(funcionarioDTO.getCargo().trim());

        if (funcionarioDTO.getEmail() != null) {
            String emailNormalizado = funcionarioDTO.getEmail().trim().toLowerCase();
            funcionarioDTO.setEmail(emailNormalizado.isEmpty() ? null : emailNormalizado);
        }

        if (funcionarioDTO.getTelefone() != null) {
            String telefoneNormalizado = funcionarioDTO.getTelefone().replaceAll("\\D", "");
            funcionarioDTO.setTelefone(telefoneNormalizado.isEmpty() ? null : telefoneNormalizado);
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

        // Verificar unicidade
        if (funcionarioRepository.existsByCpfCNPJ(funcionarioDTO.getCpfCNPJ())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        if (funcionarioRepository.existsByUsuario(funcionarioDTO.getUsuario())) {
            throw new IllegalArgumentException("Usuário já cadastrado");
        }

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

    public void deletarFuncionarioPorId(UUID id) {
        funcionarioRepository.deleteById(id);
    }
}
