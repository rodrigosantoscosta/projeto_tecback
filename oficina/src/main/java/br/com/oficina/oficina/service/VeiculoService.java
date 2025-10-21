package br.com.oficina.oficina.service;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;


    private final ClienteRepository clienteRepository;

    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    public Optional<Veiculo> buscarPorId(Long id) {
        return veiculoRepository.findById(id);
    }

    public Veiculo salvar(Veiculo veiculo, Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        veiculo.setCliente(cliente);
        return veiculoRepository.save(veiculo);
    }

    public Veiculo atualizar(Long id, Veiculo veiculoAtualizado) {
        return veiculoRepository.findById(id).map(veiculo -> {
            veiculo.setMarca(veiculoAtualizado.getMarca());
            veiculo.setModelo(veiculoAtualizado.getModelo());
            veiculo.setPlaca(veiculoAtualizado.getPlaca());
            veiculo.setAno(veiculoAtualizado.getAno());
            return veiculoRepository.save(veiculo);
        }).orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
    }

    public void excluir(Long id) {
        veiculoRepository.deleteById(id);
    }
}
