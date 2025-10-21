package br.com.oficina.oficina.model;

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


@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false)
    private TipoCliente tipoCliente;

    @Column(name = "nome", nullable = false)
    @NotBlank(message = "Nome é obrigatório")
    @Size ( max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Column(name = "razao_social")
    @Size(max = 100, message = "Razão social deve ter no máximo 100 caracteres")
    private String razaoSocial;

    @Column(unique = true, nullable = true, length = 14)
    private String cpf;

//    @Column(unique = true, nullable = true, length = 18)
//    private String cpfCnpj;//isso é horrivel


    @Column(name = "telefone", nullable = false, length = 20)
    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    //Endereço deve ser uma entidade ou usar uma API
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", referencedColumnName = "id")
    private Endereco endereco;

    @Email
    private String email;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    //CascadeType reflete as operações de cliente nos veiculos
    //orphanRemoval deleta automaticamente os veiculos associados caso um cliente seja apagado
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL /*, orphanRemoval = true*/)
    private List<Veiculo> veiculos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<Atendimento> atendimentos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}