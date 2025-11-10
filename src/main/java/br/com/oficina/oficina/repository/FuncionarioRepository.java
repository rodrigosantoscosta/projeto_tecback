package br.com.oficina.oficina.repository;

import br.com.oficina.oficina.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, UUID> {
    boolean existsByUsuario(String usuario);

    boolean existsByCpfCNPJ(String cpfCNPJ);

    Optional<Funcionario> findByCpfCNPJ(String cpfCNPJ);

    Optional<Funcionario> findByUsuario(String usuario);
    
    boolean existsByEmail(String email);
}
