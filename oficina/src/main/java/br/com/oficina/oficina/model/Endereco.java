package br.com.oficina.oficina.model;

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
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cep", nullable = false, length = 9)
    @NotBlank(message = "CEP é obrigatório")
    private String cep;

    @Column(name = "logradouro", nullable = false)
    @NotBlank(message = "Logradouro é obrigatório")
    private String logradouro;

    @Column(name = "numero", nullable = false)
    @NotBlank(message = "Número é obrigatório")
    private String numero;

    @Column(name = "complemento")
    private String complemento;

    @Column(name = "bairro", nullable = false)
    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    @Column(name = "cidade", nullable = false)
    @NotBlank(message = "Cidade é obrigatório")
    private String localidade;

    @Column(name = "estado", nullable = false, length = 2)
    @NotBlank(message = "Estado é obrigatório")
    private String uf;

}