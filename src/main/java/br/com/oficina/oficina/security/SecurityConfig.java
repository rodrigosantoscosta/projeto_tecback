package br.com.oficina.oficina.security;

import br.com.oficina.oficina.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
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
            
            // Configura o gerenciamento de sessão como STATELESS
            // Isso significa que não usaremos sessões HTTP, já que usamos JWT
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configura as autorizações das requisições HTTP
            .authorizeHttpRequests(auth -> auth
                // Permite requisições POST para /funcionarios sem autenticação
                .requestMatchers(HttpMethod.POST, "/funcionarios").permitAll()
                
                // Define URLs que podem ser acessadas sem autenticação
                .requestMatchers(
                    "/funcionarios/login",  // Endpoint de login
                    "/v3/api-docs",         // OpenAPI JSON raiz
                    "/v3/api-docs/**",      // OpenAPI sub-recursos
                    "/v3/api-docs.yaml",    // OpenAPI YAML
                    "/v3/**",               // Qualquer outro endpoint sob /v3
                    "/swagger-ui/**",       // Interface do Swagger UI
                    "/swagger-ui/index.html", // Página principal do Swagger UI
                    "/swagger-ui.html",     // Página HTML do Swagger
                    "/swagger-resources/**", // Compat (se existir)
                    "/",                    // Rota raiz
                    "/index.html",          // Página inicial
                    "/style.css",           // Arquivo CSS principal
                    "/validation.css"       // Estilos de validação
                ).permitAll()  // Permite acesso sem autenticação
                // Redundância usando AntPath para evitar qualquer conflito de matcher MVC
                .requestMatchers(
                    new AntPathRequestMatcher("/v3/**"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/swagger-ui.html")
                ).permitAll()
                
                // Todas as outras requisições precisam de autenticação
                .anyRequest().authenticated()
            )
            ;

        // Adiciona o filtro JWT antes do filtro de autenticação padrão do Spring Security
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
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
