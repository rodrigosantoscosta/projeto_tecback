package br.com.oficina.oficina.security;

import br.com.oficina.oficina.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    /**
     * Cria e configura o filtro JWT para autenticação.
     * Este filtro é responsável por processar o token JWT nas requisições.
     * 
     * @return Uma instância de JwtAuthenticationFilter configurada
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    /**
     * Configura as regras de segurança da aplicação.
     * 
     * @param http Objeto HttpSecurity para configurar as regras de segurança
     * @return SecurityFilterChain configurado
     * @throws Exception Se ocorrer um erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita a proteção CSRF (Cross-Site Request Forgery)
            // Necessário para APIs RESTful que usam autenticação stateless como JWT
            .csrf(csrf -> csrf.disable())

            // Permite que o H2 Console seja carregado em iframe (necessário para o browser)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // Configura o gerenciamento de sessão como STATELESS
            // Isso significa que não usaremos sessões HTTP, já que usamos JWT
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configura as autorizações das requisições HTTP
            .authorizeHttpRequests(auth -> {
                if (securityEnabled) {
                    // Modo com segurança ativada
                    auth
                        // Permite requisições POST para /funcionarios sem autenticação
                        .requestMatchers(HttpMethod.POST, "/funcionarios").permitAll()

                        // Define URLs que podem ser acessadas sem autenticação
                        .requestMatchers(
                            "/auth/login",          // Login
                            "/auth/refresh",        // Renovação de token
                            "/auth/logout",         // Logout (revoga refresh token sem access token)
                            "/volume/status",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/v3/api-docs.yaml",
                            "/v3/**",
                            "/swagger-ui/**",
                            "/swagger-ui/index.html",
                            "/swagger-ui.html",
                            "/swagger-resources/**",
                            "/",
                            "/index.html",
                            "/style.css",
                            "/validation.css"
                        ).permitAll()
                        .requestMatchers(
                            new AntPathRequestMatcher("/v3/**"),
                            new AntPathRequestMatcher("/swagger-ui/**"),
                            new AntPathRequestMatcher("/swagger-ui.html")
                        ).permitAll()

                        // Todas as outras requisições precisam de autenticação
                        .anyRequest().authenticated();
                } else {
                    // Modo sem segurança - permite todas as requisições
                    auth.anyRequest().permitAll();
                }
            })
            ;

        // Adiciona o filtro JWT antes do filtro de autenticação padrão do Spring Security
        // (apenas se a segurança estiver ativada)
        if (securityEnabled) {
            http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        }
        
        // Constrói e retorna a configuração de segurança
        return http.build();
    }

    /**
     * Configura o gerenciador de autenticação do Spring Security.
     * 
     * @param config Configuração de autenticação fornecida pelo Spring
     * @return AuthenticationManager configurado
     * @throws Exception Se ocorrer um erro na configuração
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura o codificador de senhas da aplicação.
     * Utiliza BCrypt como algoritmo de hash para senhas.
     * 
     * @return Uma instância de PasswordEncoder configurada com BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
