package br.com.oficina.oficina.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Utilitário para operações com JWT (JSON Web Token).
 * Responsável por gerar, validar e extrair informações de tokens JWT.
 * 
 * FLUXO:
 * 1. Gera tokens JWT com informações do usuário
 * 2. Extrai claims (informações) do token
 * 3. Valida se o token é válido (username correto e não expirado)
 * 4. Verifica se o token expirou
 * 
 * ANOTAÇÕES USADAS:
 * - @Component: Registra a classe como um bean gerenciado pelo Spring
 * - @Value: Injeta valores de propriedades do application.properties
 */
@Component
public class JwtUtil {

    /**
     * Chave secreta usada para assinar e validar tokens JWT.
     * Lida do application.properties com valor padrão.
     * IMPORTANTE: Em produção, use uma chave muito mais longa e segura!
     */
    @Value("${jwt.secret:troque-por-uma-chave-muito-secreta-com-muitos-caracteres}")
    private String secret;

    /**
     * Tempo de expiração do token em milissegundos (padrão: 1 hora = 3600000ms).
     * Lido do application.properties.
     */
    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    /**
     * Gera a chave de assinatura HMAC SHA-256 a partir da chave secreta.
     * Converte a string da chave secreta em bytes UTF-8 para criar a chave criptográfica.
     * 
     * @return Chave HMAC SHA-256 para assinar e validar tokens
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera um novo token JWT para o usuário autenticado.
     * 
     * FLUXO:
     * 1. Obtém a data/hora atual
     * 2. Calcula a data de expiração (agora + expirationMs)
     * 3. Cria um builder JWT com:
     *    - Subject: username do usuário
     *    - IssuedAt: data de criação do token
     *    - Expiration: data de expiração do token
     *    - Signature: assinatura HMAC SHA-256
     * 4. Compacta o token em formato JWT (header.payload.signature)
     * 
     * @param userDetails Detalhes do usuário autenticado
     * @return Token JWT gerado
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrai o username (subject) do token JWT.
     * 
     * @param token Token JWT
     * @return Username armazenado no token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a data de expiração do token JWT.
     * 
     * @param token Token JWT
     * @return Data de expiração do token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai um claim específico do token JWT de forma genérica.
     * 
     * FLUXO:
     * 1. Cria um parser JWT com a chave de assinatura
     * 2. Valida e faz parse do token JWT
     * 3. Extrai o corpo do token (claims)
     * 4. Aplica a função resolver para extrair o claim desejado
     * 
     * @param token Token JWT
     * @param resolver Função que extrai o claim desejado dos claims
     * @param <T> Tipo genérico do claim a ser extraído
     * @return O claim extraído
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    /**
     * Valida se o token JWT é válido para o usuário.
     * 
     * VALIDAÇÕES:
     * 1. Username no token corresponde ao username do usuário
     * 2. Token não está expirado
     * 
     * @param token Token JWT a validar
     * @param userDetails Detalhes do usuário para comparação
     * @return true se o token é válido, false caso contrário
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Verifica se o token JWT expirou.
     * Compara a data de expiração do token com a data/hora atual.
     * 
     * @param token Token JWT
     * @return true se o token expirou, false caso contrário
     */
    private boolean isTokenExpired(String token) {
        final Date exp = extractExpiration(token);
        return exp.before(new Date());
    }
}

