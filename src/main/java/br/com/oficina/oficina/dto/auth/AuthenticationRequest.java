package br.com.oficina.oficina.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationRequest {
    @NotBlank private String usuario;
    @NotBlank private String senha;
}
