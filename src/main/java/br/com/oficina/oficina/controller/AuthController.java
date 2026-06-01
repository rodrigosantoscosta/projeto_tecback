package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.auth.AuthResponse;
import br.com.oficina.oficina.dto.auth.LoginRequest;
import br.com.oficina.oficina.dto.auth.RefreshRequest;
import br.com.oficina.oficina.exception.CredenciaisInvalidasException;
import br.com.oficina.oficina.model.RefreshToken;
import br.com.oficina.oficina.service.RefreshTokenService;
import br.com.oficina.oficina.security.JwtUtil;
import br.com.oficina.oficina.security.UsuarioPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e renovação de sessão")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * POST /auth/login
     * Autentica o funcionário e retorna um par access + refresh token.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica o funcionário e retorna access + refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        log.info("Tentativa de login — usuário: {}", req.getUsuario());
        try {
            var authToken = new UsernamePasswordAuthenticationToken(req.getUsuario(), req.getSenha());
            var auth      = authenticationManager.authenticate(authToken);

            if (!(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
                throw new IllegalStateException(
                        "Principal inesperado após autenticação: " + auth.getPrincipal().getClass());
            }

            String accessToken    = jwtUtil.generateAccessToken(principal);
            RefreshToken refreshToken = refreshTokenService.criar(principal.getFuncionario());

            log.info("Login bem-sucedido — usuário: {}", req.getUsuario());
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .build());

        } catch (AuthenticationException ex) {
            // [item-2] lança exception de domínio — GlobalExceptionHandler retorna ErrorDetails 401
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }
    }

    /**
     * POST /auth/refresh
     * Troca um refresh token válido por um novo par de tokens (rotação atômica).
     */
    @PostMapping("/refresh")
    @Operation(summary = "Renovar sessão", description = "Troca um refresh token válido por um novo par de tokens")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        log.info("Requisição de refresh token");
        // [item-3] ResourceNotFoundException e IllegalStateException ambos propagam para o GlobalExceptionHandler:
        //   - ResourceNotFoundException → 404 via @ResponseStatus (token inexistente)
        //   - CredenciaisInvalidasException → 401 (token revogado ou expirado, relançado abaixo)
        try {
            RefreshToken novoRefresh = refreshTokenService.rotacionar(req.getRefreshToken());
            UsuarioPrincipal principal = UsuarioPrincipal.fromFuncionario(novoRefresh.getFuncionario());
            String novoAccess = jwtUtil.generateAccessToken(principal);

            log.info("Refresh bem-sucedido — funcionário: {}", novoRefresh.getFuncionario().getUsuario());
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(novoAccess)
                    .refreshToken(novoRefresh.getToken())
                    .build());

        } catch (IllegalStateException ex) {
            // token revogado ou expirado → 401 com ErrorDetails, mesmo contrato do login
            throw new CredenciaisInvalidasException(ex.getMessage());
        }
        // ResourceNotFoundException não é capturada aqui — sobe para o GlobalExceptionHandler → 404
    }

    /**
     * POST /auth/logout
     * Revoga o refresh token. O access token expira naturalmente em 15 min.
     * Endpoint público — cliente pode não ter access token válido ao chamar logout.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga o refresh token do funcionário")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        refreshTokenService.revogar(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
