package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.funcionario.CriarFuncionarioDTO;
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

    public List<Funcionario> listarTodosFuncionarios() {
        return funcionarioRepository.findAll();
    }

    public java.util.Optional<Funcionario> buscarPorId(UUID id) {
        return funcionarioRepository.findById(id);
    }


    public void cadastrarFuncionario(CriarFuncionarioDTO funcionarioDTO, String senhaAberta) {
        if (funcionarioDTO == null) {
            throw new IllegalArgumentException("Dados do funcionário são obrigatórios");
        }
        if (!StringUtils.hasText(senhaAberta)) {
            throw new IllegalArgumentException("Senha é obrigatória");
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

        // Normalizar CPF/CNPJ (remover caracteres não numéricos, se existir)
        if (funcionarioDTO.getCpfCNPJ() != null && !funcionarioDTO.getCpfCNPJ().isBlank()) {
            String cpfNormalizado = funcionarioDTO.getCpfCNPJ().replaceAll("\\D", "");
            funcionarioDTO.setCpfCNPJ(cpfNormalizado);
        } else {
            throw new IllegalArgumentException("CPF/CNPJ é obrigatório");
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setCargo(funcionarioDTO.getCargo());
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setTelefone(funcionarioDTO.getTelefone());
        funcionario.setCpfCNPJ(funcionarioDTO.getCpfCNPJ());
        funcionario.setUsuario(funcionarioDTO.getUsuario());
        funcionario.setSenhaHash(passwordEncoder.encode(senhaAberta));


        // Verificar unicidade
        if (funcionarioRepository.existsByCpfCNPJ(funcionarioDTO.getCpfCNPJ())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        if (funcionarioRepository.existsByUsuario(funcionarioDTO.getUsuario())) {
            throw new IllegalArgumentException("Usuário já cadastrado");
        }

        funcionarioRepository.save(funcionario);
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
