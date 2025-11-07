package br.com.oficina.oficina.dto.funcionario;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FuncionarioDTO {
    private UUID id;
    private String nome;
    private String cpfCNPJ;
    private String cargo;
    private String telefone;
    private String email;
    private String usuario;
    private LocalDateTime dataCadastro;
}
