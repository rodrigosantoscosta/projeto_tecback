package br.com.oficina.oficina.dto.funcionario;

import br.com.oficina.oficina.model.Funcionario;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class FuncionarioDTO {
    private UUID id;
    private String nome;
    private String cpfCNPJ;
    private String cargo;
    private String telefone;
    private String email;
    private String usuario;
    private LocalDateTime dataCadastro;

    // Construtor de conveniência — usado em listarTodosFuncionarios() via new FuncionarioDTO(f)
    public FuncionarioDTO(Funcionario funcionario) {
        this.id = funcionario.getId();
        this.nome = funcionario.getNome();
        this.cpfCNPJ = funcionario.getCpfCNPJ();
        this.cargo = funcionario.getCargo();
        this.telefone = funcionario.getTelefone();
        this.email = funcionario.getEmail();
        this.usuario = funcionario.getUsuario();
        this.dataCadastro = funcionario.getDataCadastro();
    }
}

