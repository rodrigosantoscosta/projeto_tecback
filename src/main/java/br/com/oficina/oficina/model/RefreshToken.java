package br.com.oficina.oficina.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Token opaque — UUID gerado aleatoriamente, armazenado em texto plano. */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(nullable = false)
    private boolean revogado;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = Instant.now();
    }

    public boolean isExpirado() {
        return Instant.now().isAfter(expiraEm);
    }

    public boolean isValido() {
        return !revogado && !isExpirado();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RefreshToken rt)) return false;
        return id != null && id.equals(rt.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
