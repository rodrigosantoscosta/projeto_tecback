package br.com.oficina.oficina.service;

import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.repository.FuncionarioRepository;
import br.com.oficina.oficina.security.UsuarioPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final FuncionarioRepository repo;

    public CustomUserDetailsService(FuncionarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public UsuarioPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        Funcionario f = repo.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return UsuarioPrincipal.fromFuncionario(f);
    }
}

