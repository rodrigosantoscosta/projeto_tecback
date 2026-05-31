package br.com.oficina.oficina.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Usuário é obrigatório")
    private String usuario;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
