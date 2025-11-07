package br.com.oficina.oficina.dto.cliente;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CadastrarVeiculoDTO {


    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @NotBlank(message = "CPF/CNPJ é obrigatório")
    private String cpfCNPJ;

    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos")
    private String cep;

    @NotBlank(message = "Número é obrigatório")
    private String numero;

    private String complemento;

    //Método que intercepta o JSON e limpa o CEP antes de atribuir
    @JsonSetter("cep")
    public void setCep(String cep) {
        this.cep = cep != null ? cep.replaceAll("\\D", "") : null;
    }

    // Método que intercepta o JSON e limpa o CPF/CNPJ antes de atribuir
    @JsonSetter("cpfCNPJ")
    public void setCpfCNPJ(String cpfCNPJ) {
        this.cpfCNPJ = cpfCNPJ != null ? cpfCNPJ.replaceAll("\\D", "") : null;
    }

    // Método que intercepta o JSON e limpa o telefone antes de atribuir
    @JsonSetter("telefone")
    public void setTelefone(String telefone) {
        this.telefone = telefone != null ? telefone.replaceAll("\\D", "") : null;
    }
}