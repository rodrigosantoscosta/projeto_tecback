package br.com.oficina.oficina.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrasilApiCepResponse {
    @JsonProperty("cep")
    private String cep;

    @JsonProperty("state")
    private String state;

    @JsonProperty("city")
    private String city;

    @JsonProperty("neighborhood")
    private String neighborhood;

    @JsonProperty("street")
    private String street;

    @JsonProperty("service")
    private String service;
    
    // A macarronada vai se formando, ja vai colocando o parmesao
    // Getters 'alias' para usar os campos de Endereco
    public String getLogradouro() {
        return street;
    }
    
    public String getBairro() {
        return neighborhood;
    }
    
    public String getLocalidade() {
        return city;
    }
    
    public String getUf() {
        return state;
    }
}
