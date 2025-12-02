package br.com.oficina.oficina.model;

import br.com.oficina.oficina.validator.annotation.CPFouCNPJ;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clientes")
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Entidade que representa um cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "ID único do cliente", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 1, max = 150)
    @Column(nullable = false, name = "nome_completo", length = 150)
    @Schema(description = "Nome completo do cliente", example = "João da Silva")
    private String nomeCompleto;

    @CPFouCNPJ
    @Column(name = "cpf_cnpj", unique = true, nullable = false, length = 14)
    @Schema(description = "CPF ou CNPJ do cliente", example = "12345678909")
    private String cpfCNPJ;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    @Column(name = "telefone")
    @Schema(description = "Telefone de contato", example = "(11) 98765-4321")
    private String telefone;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 254)
    @Column(name = "email", unique = true, nullable = false, length = 254)
    @Schema(description = "Email do cliente", example = "joao@email.com")
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id")
    @Schema(description = "Endereço do cliente")
    private Endereco endereco;

    @Column(name = "data_cadastro")
    @Schema(description = "Data de cadastro do cliente")
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonIgnore
    @Schema(description = "Lista de veículos do cliente", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Veiculo> veiculos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}