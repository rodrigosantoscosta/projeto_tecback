package br.com.oficina.oficina.dto.funcionario;

import br.com.oficina.oficina.validator.annotation.CPFouCNPJ;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CadastrarFuncionarioDTO {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;

    @CPFouCNPJ
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter apenas números")
    private String cpfCNPJ;

    @NotBlank(message = "Cargo é obrigatório")
    @Size(max = 50, message = "Cargo deve ter no máximo 50 caracteres")
    private String cargo;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 254, message = "E-mail deve ter no máximo 254 caracteres")
    private String email;

    @NotBlank(message = "Usuário é obrigatório")
    @Size(min = 3, max = 50, message = "Usuário deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-z0-9._-]+$", message = "Usuário deve conter apenas letras minúsculas, números, pontos, hífens ou sublinhados")
    private String usuario;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String senha;
}
