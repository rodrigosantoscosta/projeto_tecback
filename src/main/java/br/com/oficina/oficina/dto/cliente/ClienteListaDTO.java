package br.com.oficina.oficina.dto.cliente;

import br.com.oficina.oficina.model.Endereco;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteListaDTO {
    private UUID id;
    private String nomeCompleto;
    private String cpfCNPJ;
    private String telefone;
    private String email;
    private Endereco endereco;
    private LocalDateTime dataCadastro;
    private Integer quantidadeVeiculos;
}
