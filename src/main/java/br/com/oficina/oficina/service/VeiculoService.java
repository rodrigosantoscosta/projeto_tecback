package br.com.oficina.oficina.service;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    public void cadastrarVeiculoCompleto(Map<String, Object> requestMap) {
        // Extrai os dados do veículo
        String placa = (String) requestMap.get("placa");
        String marca = (String) requestMap.get("marca");
        String modelo = (String) requestMap.get("modelo");
        Integer ano = (Integer) requestMap.get("ano");
        String cor = (String) requestMap.get("cor");
        Double quilometragem = requestMap.get("quilometragem") != null 
            ? ((Number) requestMap.get("quilometragem")).doubleValue() 
            : null;
        String clienteIdStr = (String) requestMap.get("clienteId");

        // Normaliza a placa (remove espaços e converte para maiúsculas)
        if (placa != null) {
            placa = placa.replaceAll("\\s+", "").toUpperCase();
        }

        // Verifica se a placa já existe
        if (veiculoRepository.existsByPlaca(placa)) {
            throw new RuntimeException("Placa já cadastrada no sistema");
        }

        // Busca o cliente
        UUID clienteId = UUID.fromString(clienteIdStr);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Cria e salva o veículo
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(placa);
        veiculo.setMarca(marca);
        veiculo.setModelo(modelo);
        veiculo.setAno(ano);
        veiculo.setCor(cor);
        veiculo.setQuilometragem(quilometragem);
        veiculo.setCliente(cliente);

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
