package br.com.oficina.oficina.dto.atendimento;

import br.com.oficina.oficina.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoDTO {
    private UUID id;
    private String descricaoServico;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataConclusao;
    private StatusAtendimento status;
    private LocalDateTime dataCadastro;
    private UUID cliente;
    private String veiculo;
    private UUID funcionario;

    public AtendimentoDTO(Atendimento atendimento){
        this.id = atendimento.getId();
        this.descricaoServico = atendimento.getDescricaoServico();
        this.dataEntrada = atendimento.getDataEntrada();
        this.dataConclusao = atendimento.getDataConclusao();
        this.status = atendimento.getStatus();
        this.dataCadastro = atendimento.getDataCadastro();
        this.cliente = atendimento.getCliente().getId();
        this.veiculo = atendimento.getVeiculo().getPlaca();
        this.funcionario = atendimento.getFuncionario().getId();
    }
}
