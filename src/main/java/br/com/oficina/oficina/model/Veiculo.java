package br.com.oficina.oficina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "veiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @NotBlank(message = "Placa é obrigatória")
    @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "Placa deve estar no formato válido (AAA0A00 ou AAA0000)")
    @Column(nullable = false, unique = true, name = "placa", length = 7)
    private String placa;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50, message = "Modelo deve ter no máximo 50 caracteres")
    @Column(nullable = false, name = "modelo", length = 50)
    private String modelo;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
    @Column(nullable = false, name = "marca", length = 50)
    private String marca;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 2100, message = "Ano deve ser menor ou igual a 2100")
    @Column(nullable = false, name = "ano")
    private Integer ano;

    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    @Column(nullable = true, name = "cor", length = 30)
    private String cor;

    @Min(value = 0, message = "Quilometragem não pode ser negativa")
    @Column(nullable = true)
    private Double quilometragem;

    @Column(nullable = false, name = "data_cadastro")
    private LocalDateTime dataCadastro;

    //Carrega o cliente só quando for acessado(lazy loading)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "endereco"})
    private Cliente cliente;

    //Por padrão @OneToMany usa lazy loading, então atendimentos só é carregado
    //quando for acessado
    @OneToMany(mappedBy = "veiculo")
    @JsonIgnore
    private List<Atendimento> atendimentos;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }


}
