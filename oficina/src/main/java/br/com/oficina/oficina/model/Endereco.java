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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getLocalidade() { return localidade; }
    public void setLocalidade(String localidade) { this.localidade = localidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
}