package br.com.oficina.oficina.service;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente clienteAtualizado) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setNome(clienteAtualizado.getNome());
//            cliente.setCpfCnpj(clienteAtualizado.getCpfCnpj()); //loucura
            cliente.setEmail(clienteAtualizado.getEmail());
            cliente.setTelefone(clienteAtualizado.getTelefone());
            cliente.setTipoCliente(clienteAtualizado.getTipoCliente());
            return clienteRepository.save(cliente);
        }).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public void excluir(Long id) {
        clienteRepository.deleteById(id);
    }
}
