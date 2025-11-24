package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.exception.ClienteNaoEncontradoException;
import br.com.oficina.oficina.exception.RecursoJaCadastradoException;
import br.com.oficina.oficina.exception.VeiculoNaoEncontradoException;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Veiculo cadastrarVeiculo(CadastrarVeiculoDTO dto) {
        log.info("Cadastrando veículo - Placa: {}", dto.getPlaca());

        // Normaliza a placa
        String placaNormalizada = dto.getPlaca().replaceAll("\\s+", "").toUpperCase();
        log.debug("Placa normalizada: {}", placaNormalizada);

        // Verifica se a placa já existe
        if (veiculoRepository.existsByPlaca(placaNormalizada)) {
            log.error("Placa já cadastrada: {}", placaNormalizada);
            throw new RecursoJaCadastradoException("Placa já cadastrada no sistema");
        }

        // Busca o cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", dto.getClienteId());
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + dto.getClienteId()
                    );
                });

        log.info("Cliente encontrado: {} - {}", cliente.getId(), cliente.getNomeCompleto());

        // Cria o veículo
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placaNormalizada);
        veiculo.setModelo(dto.getModelo());
        veiculo.setMarca(dto.getMarca());
        veiculo.setAno(dto.getAno());
        veiculo.setCor(dto.getCor());
        veiculo.setQuilometragem(dto.getQuilometragem());
        veiculo.setCliente(cliente);

        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);
        log.info("Veículo cadastrado com sucesso - ID: {}, Placa: {}, Cliente: {}",
                veiculoSalvo.getId(), veiculoSalvo.getPlaca(), cliente.getNomeCompleto());

        return veiculoSalvo;
    }

    public List<Veiculo> listarTodosVeiculos() {
        log.info("Listando todos os veículos");
        return veiculoRepository.findAll();
    }

    public Veiculo buscarVeiculoPorId(UUID id) {
        log.info("Buscando veículo por ID: {}", id);
        return veiculoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado: {}", id);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + id
                    );
                });
    }

    public Veiculo buscarVeiculoPorPlaca(String placa) {
        log.info("Buscando veículo por placa: {}", placa);
        String placaNormalizada = placa.replaceAll("\\s+", "").toUpperCase();
        return veiculoRepository.findByPlaca(placaNormalizada)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado: {}", placa);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + placa
                    );
                });
    }

    public List<Veiculo> listarVeiculosPorCliente(UUID clienteId) {
        log.info("Listando veículos do cliente: {}", clienteId);
        return veiculoRepository.findVeiculoByClienteId(clienteId);
    }

    @Transactional
    public Veiculo atualizar(UUID id, CadastrarVeiculoDTO dto) {
        log.info("Atualizando veículo: {}", id);

        // Verifica se o veículo existe
        Veiculo veiculoExistente = veiculoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado: {}", id);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + id
                    );
                });

        // Normaliza a placa
        String placaNormalizada = dto.getPlaca().replaceAll("\\s+", "").toUpperCase();

        // Verifica se a nova placa já pertence a outro veículo
        if (!veiculoExistente.getPlaca().equals(placaNormalizada)
                && veiculoRepository.existsByPlaca(placaNormalizada)) {
            log.error("Placa já cadastrada para outro veículo: {}", placaNormalizada);
            throw new RecursoJaCadastradoException("Placa já cadastrada para outro veículo");
        }

        // Busca o cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", dto.getClienteId());
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + dto.getClienteId()
                    );
                });

        // Atualiza os dados
        veiculoExistente.setPlaca(placaNormalizada);
        veiculoExistente.setMarca(dto.getMarca());
        veiculoExistente.setModelo(dto.getModelo());
        veiculoExistente.setAno(dto.getAno());
        veiculoExistente.setCor(dto.getCor());
        veiculoExistente.setQuilometragem(dto.getQuilometragem());
        veiculoExistente.setCliente(cliente);

        Veiculo veiculoAtualizado = veiculoRepository.save(veiculoExistente);
        log.info("Veículo atualizado com sucesso: {}", veiculoAtualizado.getId());

        return veiculoAtualizado;
    }

    @Transactional
    public Veiculo associarVeiculoAoCliente(UUID veiculoId, UUID clienteId) {
        log.info("Associando veículo {} ao cliente {}", veiculoId, clienteId);

        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado: {}", veiculoId);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + veiculoId
                    );
                });

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", clienteId);
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + clienteId
                    );
                });

        veiculo.setCliente(cliente);
        Veiculo veiculoAtualizado = veiculoRepository.save(veiculo);

        log.info("Veículo {} associado ao cliente {} com sucesso", veiculoId, clienteId);
        return veiculoAtualizado;
    }

    @Transactional
    public void deletarVeiculoPorId(UUID id) {
        log.info("Deletando veículo: {}", id);

        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado para deleção: {}", id);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + id
                    );
                });

        veiculoRepository.delete(veiculo);
        log.info("Veículo deletado com sucesso: {}", id);
    }

    @Transactional
    public void deletarVeiculoPorPlaca(String placa) {
        log.info("Deletando veículo por placa: {}", placa);

        String placaNormalizada = placa.replaceAll("\\s+", "").toUpperCase();

        Veiculo veiculo = veiculoRepository.findByPlaca(placaNormalizada)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado para deleção - Placa: {}", placaNormalizada);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com placa: " + placaNormalizada
                    );
                });

        veiculoRepository.delete(veiculo);
        log.info("Veículo deletado com sucesso - Placa: {}", placaNormalizada);
    }

    public Long contarTotalVeiculos() {
        log.info("Contando total de veículos");
        Long total = veiculoRepository.contarTotalVeiculos();
        log.debug("Total de veículos: {}", total);
        return total;
    }
}