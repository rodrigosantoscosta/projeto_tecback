package br.com.oficina.oficina.model;

import br.com.oficina.oficina.validator.annotation.CPFouCNPJ;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clientes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 1, max = 150)
    @Column(nullable = false, name = "nome_completo", length = 150)
    private String nomeCompleto;

    @CPFouCNPJ
    @Column(name = "cpf_cnpj", unique = true, nullable = false, length = 14)
    private String cpfCNPJ;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    @Column(name = "telefone")
    private String telefone;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 254)
    @Column(name = "email", unique = true,  nullable = false, length = 254)
    private String email;

//    @NotBlank(message = "Senha é obrigatória")
//    @Size(min = 6, message = "Senha deve ter no mínimo 8 caracteres")
//    @Column(name = "senha_hash", nullable = false, length = 60)
//    private String senhaHash;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }


}