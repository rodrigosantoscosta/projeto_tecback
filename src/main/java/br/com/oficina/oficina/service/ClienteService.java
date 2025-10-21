package br.com.oficina.oficina.service;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ViaCepService viaCepService;

    public ClienteService(ClienteRepository clienteRepository, ViaCepService viaCepService) {
        this.clienteRepository = clienteRepository;
        this.viaCepService = viaCepService;
    }

    public void cadastrarCliente(Cliente cliente) {
        clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodosClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    public void deletarClientePorId(Long id) {
        clienteRepository.deleteById(id);
    }

    public void cadastrarClienteCompleto(
            String nome,
            String cpf,
            String telefone,
            String email,
            String cep,
            String numero,
            String complemento) {

        var endereco = viaCepService.buscarEConstruirEndereco(cep, numero, complemento);

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setTelefone(telefone);
        cliente.setEmail(email);
        cliente.setEndereco(endereco);

        clienteRepository.save(cliente);
    }
}
