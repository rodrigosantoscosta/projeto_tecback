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

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /** Duração do access token em ms (padrão: 15 minutos). */
    @Value("${jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera um access token JWT com claims extras (id e cargo do funcionário).
     * Duração padrão: 15 minutos.
     *
     * @param userDetails deve ser instância de {@link UsuarioPrincipal}
     */
    public String generateAccessToken(UserDetails userDetails) {
        UsuarioPrincipal principal = (UsuarioPrincipal) userDetails;
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessExpirationMs);

        // Cargo é a única authority mapeada em UsuarioPrincipal.fromFuncionario()
        String cargo = principal.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .claim("id",    principal.getId().toString())
                .claim("cargo", cargo)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extrai o username (subject) do token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extrai a data de expiração do token. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    /** Valida assinatura, username e expiração. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}

