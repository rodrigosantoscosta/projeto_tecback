package br.com.oficina.oficina.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "veiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "placa")
    private String placa;

    @Column(nullable = false, name = "modelo")
    private String modelo;

    @Column(nullable = false, name = "marca")
    private String marca;

    @Column(nullable = true, name = "ano")
    private Integer ano;

    @Column(nullable = true)
    private Double quilometragem;

    @Column(nullable = false, name = "data_cadastro")
    private LocalDateTime dataCadastro;

    //Carrega o cliente só quando for acessado(lazy loading)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    //Por padrão @OneToMany usa lazy loading, então atendimentos só é carregado
    //quando for acessado
    @OneToMany(mappedBy = "veiculo")
    private List<Atendimento> atendimentos;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}
