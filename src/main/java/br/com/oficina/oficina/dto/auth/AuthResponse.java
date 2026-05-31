package br.com.oficina.oficina.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * Resposta unificada para login e refresh.
 * O frontend sempre recebe accessToken + refreshToken juntos.
 */
@Data
@Builder
public class AuthResponse {

    /** JWT de curta duração (15 min) usado nos requests da API. */
    private String accessToken;

    /** Token opaque de longa duração (7 dias) usado apenas em POST /auth/refresh. */
    private String refreshToken;

    /** Sempre "Bearer" — facilita leitura no cliente. */
    private final String tokenType = "Bearer";
}
