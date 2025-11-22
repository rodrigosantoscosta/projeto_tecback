package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.exception.ClienteComVeiculosException;
import br.com.oficina.oficina.exception.ClienteNaoEncontradoException;
import br.com.oficina.oficina.exception.RecursoJaCadastradoException;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.repository.ClienteRepository;
import br.com.oficina.oficina.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ViaCepService viaCepService;

    @Transactional
    public Cliente cadastrarCliente(CadastrarClienteDTO clienteDTO) {
        log.info("Iniciando cadastro de cliente: {}", clienteDTO.getNomeCompleto());

        // Normaliza CPF/CNPJ
        String cpfCNPJ = clienteDTO.getCpfCNPJ().replaceAll("\\D", "");
        log.debug("CPF/CNPJ normalizado: {}", cpfCNPJ);

        // Valida unicidade
        if (clienteRepository.existsByCpfCNPJ(cpfCNPJ)) {
            log.error("CPF/CNPJ já cadastrado: {}", cpfCNPJ);
            throw new RecursoJaCadastradoException("CPF/CNPJ já cadastrado no sistema");
        }

        if (clienteRepository.existsByEmail(clienteDTO.getEmail())) {
            log.error("Email já cadastrado: {}", clienteDTO.getEmail());
            throw new RecursoJaCadastradoException("Email já cadastrado no sistema");
        }

        // Busca endereço via CEP
        var endereco = viaCepService.buscarEConstruirEndereco(
                clienteDTO.getCep(),
                clienteDTO.getNumero(),
                clienteDTO.getComplemento()
        );

        if (endereco == null) {
            log.error("Endereço não encontrado para CEP: {}", clienteDTO.getCep());
            throw new RuntimeException("CEP não encontrado");
        }

        // Cria e salva o cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto(clienteDTO.getNomeCompleto().trim());
        cliente.setCpfCNPJ(cpfCNPJ);
        cliente.setTelefone(clienteDTO.getTelefone().trim());
        cliente.setEmail(clienteDTO.getEmail().trim().toLowerCase());
        cliente.setEndereco(endereco);

        Cliente clienteSalvo = clienteRepository.save(cliente);
        log.info("Cliente cadastrado com sucesso - ID: {}", clienteSalvo.getId());

        return clienteSalvo;
    }

    public List<Cliente> listarTodosClientes() {
        log.info("Listando todos os clientes");
        List<Cliente> clientes = clienteRepository.findAll();
        log.debug("Total de clientes encontrados: {}", clientes.size());
        return clientes;
    }

    public Cliente buscarClientePorId(UUID id) {
        log.debug("Buscando cliente por ID: {}", id);
        return clienteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado: {}", id);
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + id
                    );
                });
    }

    public Optional<Cliente> buscarClientePorCpfCNPJ(String cpf) {
        log.debug("Buscando cliente por CPF: {}", cpf);
        String cpfNormalizado = cpf.replaceAll("\\D", "");
        log.trace("CPF normalizado: {}", cpfNormalizado);
        return clienteRepository.findByCpfCNPJ(cpfNormalizado);
    }

    @Transactional
    public void deletarClientePorId(UUID id) {
        log.info("Deletando cliente: {}", id);

        // Verifica se o cliente existe
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado para deleção: {}", id);
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + id
                    );
                });

        // Verifica se há veículos associados
        long quantidadeVeiculos = veiculoRepository.countByClienteId(id);

        if (quantidadeVeiculos > 0) {
            log.warn("Tentativa de deletar cliente {} com {} veículo(s)", id, quantidadeVeiculos);
            throw new ClienteComVeiculosException(
                    String.format(
                            "Não é possível deletar o cliente. Existem %d veículo(s) associado(s). " +
                                    "Remova ou transfira os veículos antes de deletar o cliente.",
                            quantidadeVeiculos
                    )
            );
        }

        clienteRepository.delete(cliente);
        log.info("Cliente deletado com sucesso: {}", id);
    }
}