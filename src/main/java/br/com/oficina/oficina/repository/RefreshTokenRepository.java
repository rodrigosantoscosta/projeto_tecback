package br.com.oficina.oficina.repository;

import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    /** Revoga todos os tokens ativos de um funcionário (usado no logout e no login). */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revogado = true WHERE rt.funcionario = :funcionario AND rt.revogado = false")
    void revogarTodosPorFuncionario(Funcionario funcionario);

    /** Limpeza periódica — remove tokens expirados ou revogados. */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revogado = true OR rt.expiraEm < CURRENT_TIMESTAMP")
    void deletarExpiradosERevogados();
}
