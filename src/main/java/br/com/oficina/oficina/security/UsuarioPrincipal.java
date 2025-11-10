package br.com.oficina.oficina.security;

import br.com.oficina.oficina.model.Funcionario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UsuarioPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    private UsuarioPrincipal(UUID id, String username, String password, List<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UsuarioPrincipal fromFuncionario(Funcionario f) {
        List<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority(f.getCargo()));
        return new UsuarioPrincipal(f.getId(), f.getUsuario(), f.getSenhaHash(), auth);
    }

    public UUID getId() {
        return id;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}