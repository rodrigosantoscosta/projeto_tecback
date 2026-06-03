package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.dto.veiculo.VeiculoDTO;
import br.com.oficina.oficina.exception.ClienteNaoEncontradoException;
import br.com.oficina.oficina.exception.RecursoJaCadastradoException;
import br.com.oficina.oficina.exception.VeiculoComAtendimentosException;
import br.com.oficina.oficina.exception.VeiculoNaoEncontradoException;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.AtendimentoRepository;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final AtendimentoRepository atendimentoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository,
                          AtendimentoRepository atendimentoRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
        this.atendimentoRepository = atendimentoRepository;
    }

    @Transactional
    public VeiculoDTO cadastrarVeiculo(CadastrarVeiculoDTO dto) {
        log.info("Cadastrando veículo - Placa: {}", dto.getPlaca());

        String placaNormalizada = dto.getPlaca().replaceAll("\\s+", "").toUpperCase();
        log.debug("Placa normalizada: {}", placaNormalizada);

        if (veiculoRepository.existsByPlaca(placaNormalizada)) {
            log.error("Placa já cadastrada: {}", placaNormalizada);
            throw new RecursoJaCadastradoException("Placa já cadastrada no sistema");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", dto.getClienteId());
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + dto.getClienteId()
                    );
                });

        log.info("Cliente encontrado: {} - {}", cliente.getId(), cliente.getNomeCompleto());

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

        return toDTO(veiculoSalvo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarTodosVeiculos() {
        log.info("Listando todos os veículos");
        List<Veiculo> veiculos = veiculoRepository.findAll();
        return veiculos.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public VeiculoDTO buscarVeiculoPorId(UUID id) {
        log.info("Buscando veículo por ID: {}", id);
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado: {}", id);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + id
                    );
                });
        return toDTO(veiculo);
    }

    @Transactional(readOnly = true)
    public VeiculoDTO buscarVeiculoPorPlaca(String placa) {
        log.info("Buscando veículo por placa: {}", placa);
        String placaNormalizada = placa.replaceAll("\\s+", "").toUpperCase();
        Veiculo veiculo = veiculoRepository.findByPlaca(placaNormalizada)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado: {}", placa);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + placa
                    );
                });
        return toDTO(veiculo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO> listarVeiculosPorCliente(UUID clienteId) {
        log.info("Listando veículos do cliente: {}", clienteId);
        List<Veiculo> veiculos = veiculoRepository.findVeiculoByClienteId(clienteId);
        return veiculos.stream().map(this::toDTO).toList();
    }

    @Transactional
    public VeiculoDTO atualizarVeiculo(UUID id, CadastrarVeiculoDTO dto) {
        log.info("Atualizando veículo: {}", id);

        Veiculo veiculoExistente = veiculoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Veículo não encontrado: {}", id);
                    return new VeiculoNaoEncontradoException(
                            "Veículo não encontrado com ID: " + id
                    );
                });

        String placaNormalizada = dto.getPlaca().replaceAll("\\s+", "").toUpperCase();

        if (!veiculoExistente.getPlaca().equals(placaNormalizada)
                && veiculoRepository.existsByPlaca(placaNormalizada)) {
            log.error("Placa já cadastrada para outro veículo: {}", placaNormalizada);
            throw new RecursoJaCadastradoException("Placa já cadastrada para outro veículo");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", dto.getClienteId());
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + dto.getClienteId()
                    );
                });

        veiculoExistente.setPlaca(placaNormalizada);
        veiculoExistente.setMarca(dto.getMarca());
        veiculoExistente.setModelo(dto.getModelo());
        veiculoExistente.setAno(dto.getAno());
        veiculoExistente.setCor(dto.getCor());
        veiculoExistente.setQuilometragem(dto.getQuilometragem());
        veiculoExistente.setCliente(cliente);

        Veiculo veiculoAtualizado = veiculoRepository.save(veiculoExistente);
        log.info("Veículo atualizado com sucesso: {}", veiculoAtualizado.getId());

        return toDTO(veiculoAtualizado);
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

        long quantidadeAtendimentos = atendimentoRepository.countByVeiculoId(id);

        if (quantidadeAtendimentos > 0) {
            log.warn("Tentativa de deletar veículo {} com {} atendimento(s)", id, quantidadeAtendimentos);
            throw new VeiculoComAtendimentosException(
                    String.format(
                            "Não é possível deletar o veículo. Existem %d atendimento(s) associado(s). " +
                                    "Remova ou encerre os atendimentos antes de deletar o veículo.",
                            quantidadeAtendimentos
                    )
            );
        }

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

    @Transactional(readOnly = true)
    public Long contarTotalVeiculos() {
        log.info("Contando total de veículos");
        Long total = veiculoRepository.contarTotalVeiculos();
        log.debug("Total de veículos: {}", total);
        return total;
    }

    private VeiculoDTO toDTO(Veiculo veiculo) {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setId(veiculo.getId());
        dto.setPlaca(veiculo.getPlaca());
        dto.setMarca(veiculo.getMarca());
        dto.setModelo(veiculo.getModelo());
        dto.setAno(veiculo.getAno());
        dto.setCor(veiculo.getCor());
        dto.setQuilometragem(veiculo.getQuilometragem());
        dto.setDataCadastro(veiculo.getDataCadastro());
        dto.setClienteId(veiculo.getCliente().getId());
        return dto;
    }
}
