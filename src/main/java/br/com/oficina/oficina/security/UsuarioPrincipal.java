package br.com.oficina.oficina.security;

import br.com.oficina.oficina.model.Funcionario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


public class UsuarioPrincipal implements UserDetails {

    // Informações do usuário
    private final UUID id;                              // ID único do usuário
    private final String username;                      // Nome de usuário para login
    private final String password;                      // Senha hash (BCrypt)
    private final List<GrantedAuthority> authorities;   // Permissões/roles do usuário

    /**
     * @param id ID único do usuário
     * @param username Nome de usuário
     * @param password Senha hash
     * @param authorities Lista de autoridades/permissões
     */
    private UsuarioPrincipal(UUID id, String username, String password, List<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Método factory para criar UsuarioPrincipal a partir de um Funcionario.
     * Converte a entidade Funcionario em um objeto UserDetails do Spring Security.
     *
     * FLUXO:
     * 1. Extrai o cargo do funcionário
     * 2. Cria uma autoridade (GrantedAuthority) com o cargo
     * 3. Cria e retorna uma nova instância de UsuarioPrincipal
     *
     * @param f Entidade Funcionario do banco de dados
     * @return Instância de UsuarioPrincipal pronta para autenticação
     */
    public static UsuarioPrincipal fromFuncionario(Funcionario f) {
        // Cria uma lista com uma única autoridade baseada no cargo do funcionário
        List<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority(f.getCargo()));
        return new UsuarioPrincipal(f.getId(), f.getUsuario(), f.getSenhaHash(), auth);
    }

    /**
     * Retorna o ID único do usuário.
     *
     * @return UUID do usuário
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retorna as autoridades/permissões do usuário.
     * Implementação de UserDetails.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Retorna a senha hash do usuário.
     * Implementação de UserDetails.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Retorna o nome de usuário.
     * Implementação de UserDetails.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Verifica se a conta não está expirada.
     * Implementação de UserDetails - sempre retorna true nesta aplicação.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Verifica se a conta não está bloqueada.
     * Implementação de UserDetails - sempre retorna true nesta aplicação.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Verifica se as credenciais não estão expiradas.
     * Implementação de UserDetails - sempre retorna true nesta aplicação.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Verifica se o usuário está habilitado.
     * Implementação de UserDetails - sempre retorna true nesta aplicação.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
