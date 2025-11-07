package br.com.oficina.oficina.dto.cliente;

import br.com.oficina.oficina.dto.endereco.EnderecoDTO;
import br.com.oficina.oficina.model.Endereco;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private UUID id;
    private String nomeCompleto;
    private String cpfCNPJ;
    private String telefone;
    private String email;
    private EnderecoDTO endereco;
    private LocalDateTime dataCadastro;

    public ClienteDTO(UUID id, String nomeCompleto, String cpfCNPJ, String telefone,
                      String email, Endereco endereco, LocalDateTime dataCadastro) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.cpfCNPJ = cpfCNPJ;
        this.telefone = telefone;
        this.email = email;
        this.dataCadastro = dataCadastro;

        // Converte Endereco para EnderecoDTO
        if (endereco != null) {
            this.endereco = new EnderecoDTO(
                    endereco.getId(),
                    endereco.getCep(),
                    endereco.getLogradouro(),
                    endereco.getNumero(),
                    endereco.getComplemento(),
                    endereco.getBairro(),
                    endereco.getLocalidade(),
                    endereco.getUf()
            );
        }
    }
}
