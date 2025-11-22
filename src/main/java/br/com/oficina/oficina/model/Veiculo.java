package br.com.oficina.oficina.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidade que representa um veículo")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único do veículo", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotBlank(message = "Placa é obrigatória")
    @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "Placa deve estar no formato válido (AAA0A00 ou AAA0000)")
    @Column(nullable = false, unique = true, name = "placa", length = 7)
    @Schema(description = "Placa do veículo", example = "ABC1D23")
    private String placa;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50, message = "Modelo deve ter no máximo 50 caracteres")
    @Column(nullable = false, name = "modelo", length = 50)
    @Schema(description = "Modelo do veículo", example = "Civic")
    private String modelo;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
    @Column(nullable = false, name = "marca", length = 50)
    @Schema(description = "Marca do veículo", example = "Honda")
    private String marca;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 2100, message = "Ano deve ser menor ou igual a 2100")
    @Column(nullable = false, name = "ano")
    @Schema(description = "Ano de fabricação", example = "2020")
    private Integer ano;

    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    @Column(nullable = true, name = "cor", length = 30)
    @Schema(description = "Cor do veículo", example = "Preto")
    private String cor;

    @Min(value = 0, message = "Quilometragem não pode ser negativa")
    @Column(nullable = true)
    @Schema(description = "Quilometragem atual", example = "50000")
    private Double quilometragem;

    @Column(nullable = false, name = "data_cadastro")
    @Schema(description = "Data de cadastro do veículo")
    private LocalDateTime dataCadastro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)  // OBRIGATÓRIO
    @JsonBackReference
    @Schema(description = "Cliente proprietário do veículo", required = true)
    private Cliente cliente;

    @OneToMany(mappedBy = "veiculo", fetch = FetchType.LAZY)
    @JsonIgnore
    @Schema(hidden = true)
    private List<Atendimento> atendimentos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}