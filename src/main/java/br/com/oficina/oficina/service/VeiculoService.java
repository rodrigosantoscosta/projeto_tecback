package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    public void cadastrarVeiculo(Veiculo veiculo) {
        veiculoRepository.save(veiculo);
    }

    public List<Veiculo> listarTodosVeiculos() {
        return veiculoRepository.findAll();
    }

    public Optional<Veiculo> buscarVeiculoPorId(UUID id) {
        return veiculoRepository.findById(id);
    }

    public Optional<Veiculo> buscarVeiculoPorPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa);
    }

    public List<Veiculo> listarVeiculosPorCliente(UUID clienteId) {
        return veiculoRepository.findVeiculoByClienteId(clienteId);
    }

    public void deletarVeiculoPorId(UUID id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        veiculoRepository.delete(veiculo);
    }

    public void deletarVeiculoPorPlaca(String placa) {
        Veiculo veiculo = veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        veiculoRepository.delete(veiculo);
    }

    public void cadastrarVeiculo(CadastrarVeiculoDTO cadastrarVeiculoDTO) {
        Cliente cliente = clienteRepository.findById(cadastrarVeiculoDTO.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));


        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(cadastrarVeiculoDTO.getPlaca());
        veiculo.setMarca(cadastrarVeiculoDTO.getMarca());
        veiculo.setModelo(cadastrarVeiculoDTO.getModelo());
        veiculo.setAno(cadastrarVeiculoDTO.getAno());
        veiculo.setCor(cadastrarVeiculoDTO.getCor());
        veiculo.setQuilometragem(cadastrarVeiculoDTO.getQuilometragem());
        veiculo.setCliente(cliente);

        // Verifica se a placa já existe
        if (veiculoRepository.existsByPlaca(veiculo.getPlaca())) {
            throw new RuntimeException("Placa já cadastrada no sistema");
        }

        veiculoRepository.save(veiculo);
    }

    public void associarVeiculoAoCliente(UUID veiculoId, UUID clienteId) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        veiculo.setCliente(cliente);
        veiculoRepository.save(veiculo);
    }

    public Long contarTotalVeiculos() {
        return veiculoRepository.contarTotalVeiculos();
    }
}
