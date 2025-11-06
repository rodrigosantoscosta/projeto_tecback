package br.com.oficina.oficina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String cpf;

    @Column(unique = true, nullable = false, length = 50)
    private String usuario;

    @Column(name = "senha_hash", nullable = false, length = 60)
    @Size(min = 8)
    @JsonIgnore
    private String senhaHash;

    @Column(nullable = false)
    private String cargo;

    private String telefone;

    private String email;
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "funcionario")
    private java.util.List<Atendimento> atendimentos;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}