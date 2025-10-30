package br.com.oficina.oficina.repository;

import br.com.oficina.oficina.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {
    Optional<Veiculo> findByPlaca(String placa);
    List<Veiculo> findVeiculoByClienteId(UUID clienteId);
    boolean existsByPlaca(String placa);

    //Consultas JPQL : Contar total de veiculos veiculos
    @Query("SELECT COUNT(veiculo) FROM Veiculo veiculo")
    Long contarTotalVeiculos();
}
