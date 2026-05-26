package br.com.oficina.oficina.dto.atendimento;

import br.com.oficina.oficina.model.StatusAtendimento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor


public class CadastrarAtendimentoDTO {


    @NotNull(message = "A descrição do serviço é obrigatória")
    @Schema(description = "Descreva os serviços realizados no atendimento", example = "Troca de óleo e filtro", required = true)
    private String descricaoServico;


    @Schema(description = "Descreva a situação dos serviços realizados no atendimento", example = "AGUARDANDO, ANDAMENTO, CONCLUIDO, CANCELADO", required = true)
    private StatusAtendimento statusAtendimento;

    @JsonIgnore
    @Schema(description = "Data de entrada do atendimento", example = "2024-08-15T10:30:00")
    private LocalDateTime dataConclusao;

    @NotNull(message = "ID do cliente é obrigatório")
    @Schema(description = "ID do cliente proprietário", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID clienteId;

    @NotNull(message = "Placa do Veiculo é obrigatório")
    @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "Placa deve estar no formato válido (AAA0A00 ou AAA0000)")
    @Schema(description = "Placa do veiculo proprietário", example = "FBA5D27", required = true)
    private String veiculoPlaca;

    @NotNull(message = "ID do Funcionario é obrigatório")
    @Schema(description = "ID do Funcionario proprietário", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID funcionarioId;

}