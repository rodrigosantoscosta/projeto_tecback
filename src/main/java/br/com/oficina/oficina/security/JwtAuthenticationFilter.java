package br.com.oficina.oficina.security;

import br.com.oficina.oficina.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

/**
 * Filtro de autenticação JWT que processa tokens JWT em cada requisição HTTP.
 * 
 * FLUXO:
 * 1. Extrai o header "Authorization" da requisição
 * 2. Valida se o token começa com "Bearer "
 * 3. Extrai o username do token JWT
 * 4. Carrega os detalhes do usuário do banco de dados
 * 5. Valida se o token é válido (username correto e não expirado)
 * 6. Define o usuário autenticado no SecurityContext
 * 7. Passa a requisição para o próximo filtro
 * 
 * ANOTAÇÕES USADAS:
 * - @Override: Sobrescreve o método doFilterInternal da classe pai OncePerRequestFilter
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Construtor que injeta as dependências necessárias para validação JWT.
     * 
     * @param jwtUtil Utilitário para operações com JWT
     * @param userDetailsService Serviço para carregar detalhes do usuário
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Método principal do filtro que processa cada requisição HTTP.
     * Executado uma única vez por requisição (garantido por OncePerRequestFilter).
     * 
     * FLUXO DETALHADO:
     * 1. Obtém o header "Authorization" da requisição
     * 2. Se existir e começar com "Bearer ", extrai o token (remove os 7 primeiros caracteres)
     * 3. Tenta extrair o username do token usando JwtUtil
     * 4. Se o username foi extraído com sucesso e não há autenticação prévia:
     *    - Carrega os detalhes do usuário do banco de dados
     *    - Valida se o token é válido (username correto e não expirado)
     *    - Se válido, cria um token de autenticação e o armazena no SecurityContext
     * 5. Passa a requisição para o próximo filtro na cadeia
     * 
     * @param request Requisição HTTP recebida
     * @param response Resposta HTTP a ser enviada
     * @param filterChain Cadeia de filtros para passar a requisição adiante
     * @throws ServletException Se ocorrer erro no servlet
     * @throws IOException Se ocorrer erro de I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extrai o header "Authorization" da requisição
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // Verifica se o header existe e começa com "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Remove "Bearer " (7 caracteres) e obtém apenas o token
            token = authHeader.substring(7);
            try {
                // Extrai o username do token JWT
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // Token inválido ou expirado -> segue sem autenticação
                // Permite que a requisição continue sem autenticação
            }
        }

        // Se conseguiu extrair o username e ainda não há autenticação no contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carrega os detalhes do usuário do banco de dados
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Valida se o token é válido (username correto e não expirado)
            if (jwtUtil.isTokenValid(token, userDetails)) {
                // Cria um token de autenticação com as informações do usuário
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // Adiciona detalhes da requisição ao token (IP, sessão, etc)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Armazena o token autenticado no SecurityContext
                // Isso torna o usuário autenticado para o resto da requisição
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Passa a requisição para o próximo filtro na cadeia
        filterChain.doFilter(request, response);
    }
}

