package br.com.oficina.oficina.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enderecos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidade que representa um endereço")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do endereço")
    private Long id;

    @Column(name = "cep", nullable = false, length = 9)
    @NotBlank(message = "CEP é obrigatório")
    @Schema(description = "CEP", example = "12345-678")
    private String cep;

    @Column(name = "logradouro", nullable = false)
    @NotBlank(message = "Logradouro é obrigatório")
    @Schema(description = "Nome da rua/avenida", example = "Rua das Flores")
    private String logradouro;

    @Column(name = "numero", nullable = false)
    @NotBlank(message = "Número é obrigatório")
    @Schema(description = "Número do imóvel", example = "123")
    private String numero;

    @Column(name = "complemento")
    @Schema(description = "Complemento do endereço", example = "Apto 45")
    private String complemento;

    @Column(name = "bairro", nullable = false)
    @NotBlank(message = "Bairro é obrigatório")
    @Schema(description = "Bairro", example = "Centro")
    private String bairro;

    @Column(name = "cidade", nullable = false)
    @NotBlank(message = "Cidade é obrigatório")
    @Schema(description = "Cidade", example = "São Paulo")
    private String localidade;

    @Column(name = "estado", nullable = false, length = 2)
    @NotBlank(message = "Estado é obrigatório")
    @Schema(description = "Estado (UF)", example = "SP")
    private String uf;
}