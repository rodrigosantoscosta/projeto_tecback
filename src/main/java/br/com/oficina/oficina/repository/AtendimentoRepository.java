package br.com.oficina.oficina.repository;

import br.com.oficina.oficina.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, UUID> {
    Optional<Atendimento> findById(UUID id);
    List<Atendimento> findByClienteId(UUID clienteId);

    @Query("SELECT a FROM Atendimento a WHERE a.status = 'CONCLUIDO'")
    List<Atendimento> findByStatusConcluido();

    @Query("SELECT a FROM Atendimento a ORDER BY a.dataEntrada DESC")
    List<Atendimento> findAllOrderByDataEntradaDesc();
}
