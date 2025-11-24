package br.com.oficina.oficina.dto.veiculo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para cadastro de veículo")
public class CadastrarVeiculoDTO {

    @NotBlank(message = "Placa é obrigatória")
    @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "Placa deve estar no formato válido (AAA0A00 ou AAA0000)")
    @Schema(description = "Placa do veículo", example = "ABC1D23", required = true)
    private String placa;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50, message = "Modelo deve ter no máximo 50 caracteres")
    @Schema(description = "Modelo do veículo", example = "Civic", required = true)
    private String modelo;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
    @Schema(description = "Marca do veículo", example = "Honda", required = true)
    private String marca;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 2100, message = "Ano deve ser menor ou igual a 2100")
    @Schema(description = "Ano de fabricação", example = "2020", required = true)
    private Integer ano;

    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    @Schema(description = "Cor do veículo", example = "Preto")
    private String cor;

    @Min(value = 0, message = "Quilometragem não pode ser negativa")
    @Schema(description = "Quilometragem atual", example = "50000")
    private Double quilometragem;

    @NotNull(message = "ID do cliente é obrigatório")
    @Schema(description = "ID do cliente proprietário", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID clienteId;
}