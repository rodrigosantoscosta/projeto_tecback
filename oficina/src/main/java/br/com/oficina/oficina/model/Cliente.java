package br.com.oficina.oficina.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
    private TipoCliente tipoCliente; // FISICA ou JURIDICA

    @Column(nullable = true)
    private String nome; // Para pessoa física

    @Column(nullable = true)
    private String razaoSocial; // Para pessoa jurídica

    @Column(unique = true, nullable = true, length = 14)
    private String cpf;

    @Column(unique = true, nullable = true, length = 18)
    private String cnpj;

    @Column(length = 20)
    private String telefone;

    private String endereco;

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