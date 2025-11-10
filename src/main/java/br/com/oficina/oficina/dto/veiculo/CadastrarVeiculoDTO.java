package br.com.oficina.oficina.dto.veiculo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
public class CadastrarVeiculoDTO {
    @NotBlank(message = "Placa é obrigatória")
    private String placa;

    @NotBlank(message = "Marca é obrigatória")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior que 1900")
    private Integer ano;

    private String cor;
    private Double quilometragem;

    @NotNull(message = "ID do cliente é obrigatório")
    private UUID clienteId;

    public void setPlaca(String placa) {
        this.placa = placa != null ? placa.toUpperCase() : null;
    }
}