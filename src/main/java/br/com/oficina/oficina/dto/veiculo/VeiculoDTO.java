package br.com.oficina.oficina.dto.veiculo;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.UUID;

public class VeiculoDTO {
    private UUID id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private Double quilometragem;
    private UUID clienteId;
}
