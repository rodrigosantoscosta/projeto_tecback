package br.com.oficina.oficina.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoBasicoResponse {
    private String logradouro;
    private String bairro;
    private String localidade;
    private String uf;
}