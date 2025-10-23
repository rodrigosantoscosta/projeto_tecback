package br.com.oficina.oficina.service;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    public Optional<Cliente> buscarClientePorId(UUID id) {
        return clienteRepository.findById(id);
    }

    public void deletarClientePorId(UUID id) {
        // Verifica se o cliente existe
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado "));
        clienteRepository.delete(cliente);
    }

    public void cadastrarClienteCompleto(Map<String, Object> requestMap) {
        //Ideal seria utilizar um metodo DTO, mas pra praticidade este metodo pega o JSON e converte para String usando
        // casting (String)
        String nomeCompleto = (String) requestMap.get("nomeCompleto");
        String cpfCNPJ = (String) requestMap.get("cpfCNPJ");
        String telefone = (String) requestMap.get("telefone");
        String email = (String) requestMap.get("email");
        String cep = (String) requestMap.get("cep");
        String numero = (String) requestMap.get("numero");
        String complemento = (String) requestMap.get("complemento");


        // Remove caracteres não numéricos
        if (cpfCNPJ != null) {
            cpfCNPJ = cpfCNPJ.replaceAll("\\D", "");
        }

        // Busca endereço via CEP
        var endereco = viaCepService.buscarEConstruirEndereco(cep, numero, complemento);

        // Cria e salva o cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto(nomeCompleto);
        cliente.setCpfCNPJ(cpfCNPJ);
        cliente.setTelefone(telefone);
        cliente.setEmail(email);
        cliente.setEndereco(endereco);

        clienteRepository.save(cliente);

    }
}
