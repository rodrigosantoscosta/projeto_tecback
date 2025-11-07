package br.com.oficina.oficina.model;

import br.com.oficina.oficina.validator.annotation.CPFouCNPJ;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "funcionarios")
@Data
@ToString(exclude = "senhaHash")
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @CPFouCNPJ
    @Column(name = "cpf_cnpj", unique = true, nullable = false, length = 14)
    private String cpfCNPJ;

    @NotBlank(message = "Usuário é obrigatório")
    @Size(min = 3, max = 50, message = "Usuário deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-z0-9._-]+$", message = "Usuário deve conter apenas letras minúsculas, números, pontos, hífens ou sublinhados")
    @Column(unique = true, nullable = false, length = 50)
    private String usuario;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Column(name = "senha_hash", nullable = false, length = 60)
    @JsonIgnore
    private String senhaHash;

    @NotBlank(message = "Cargo é obrigatório")
    @Size(max = 50, message = "Cargo deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String cargo;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 254, message = "E-mail deve ter no máximo 254 caracteres")
    private String email;

    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "funcionario")
    private java.util.List<Atendimento> atendimentos;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}