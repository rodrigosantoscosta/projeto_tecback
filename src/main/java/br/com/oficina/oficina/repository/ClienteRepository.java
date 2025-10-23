package br.com.oficina.oficina.repository;

import br.com.oficina.oficina.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByCpfCNPJ(String cpfCNPJ);
    Optional<Cliente> findByEmail(String email);

    boolean existsByCpfCNPJ(String cpfCNPJ);
    boolean existsByEmail(String email);
}
