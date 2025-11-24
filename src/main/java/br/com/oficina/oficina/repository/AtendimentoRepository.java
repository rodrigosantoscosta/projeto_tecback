package br.com.oficina.oficina.repository;

import br.com.oficina.oficina.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {
    Optional<Atendimento> findById(Long id);
    List<Atendimento> findByClienteId(UUID clienteId);
    List<Atendimento> findByVeiculoPlaca(String placa);
}
