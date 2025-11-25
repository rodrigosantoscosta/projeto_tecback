package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.exception.CepNaoEncontradoException;
import br.com.oficina.oficina.exception.ClienteComVeiculosException;
import br.com.oficina.oficina.dto.cliente.ClienteListaDTO;
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
import java.util.stream.Collectors;

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
            throw new CepNaoEncontradoException(clienteDTO.getCep());
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

    public List<ClienteListaDTO> listarTodosClientes() {
        log.info("Listando todos os clientes");
        List<Cliente> clientes = clienteRepository.findAll();
        log.debug("Total de clientes encontrados: {}", clientes.size());
        
        return clientes.stream()
                .map(cliente -> new ClienteListaDTO(
                        cliente.getId(),
                        cliente.getNomeCompleto(),
                        cliente.getCpfCNPJ(),
                        cliente.getTelefone(),
                        cliente.getEmail(),
                        cliente.getEndereco(),
                        cliente.getDataCadastro(),
                        cliente.getVeiculos() != null ? cliente.getVeiculos().size() : 0
                ))
                .collect(Collectors.toList());
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

    @Transactional
    public Cliente atualizarCliente(UUID id, CadastrarClienteDTO clienteDTO) {
        log.info("Atualizando cliente com ID: {}", id);
        
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado para atualização: {}", id);
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com ID: " + id
                    );
                });

        // Atualiza os dados básicos do cliente
        clienteExistente.setNomeCompleto(clienteDTO.getNomeCompleto().trim());
        clienteExistente.setCpfCNPJ(clienteDTO.getCpfCNPJ().replaceAll("\\D", ""));
        clienteExistente.setTelefone(clienteDTO.getTelefone().trim());
        clienteExistente.setEmail(clienteDTO.getEmail().trim().toLowerCase());

        // Verifica se o CEP foi alterado ou se é uma atualização de endereço
        if (!clienteExistente.getEndereco().getCep().equals(clienteDTO.getCep()) ||
                !clienteExistente.getEndereco().getNumero().equals(clienteDTO.getNumero()) ||
                !clienteExistente.getEndereco().getComplemento().equals(clienteDTO.getComplemento())) {
            
            log.debug("Atualizando endereço do cliente ID: {}", id);
            
            // Busca o novo endereço via CEP
            var novoEndereco = viaCepService.buscarEConstruirEndereco(
                    clienteDTO.getCep(),
                    clienteDTO.getNumero(),
                    clienteDTO.getComplemento()
            );

            if (novoEndereco == null) {
                log.error("Endereço não encontrado para CEP: {}", clienteDTO.getCep());
                throw new CepNaoEncontradoException(clienteDTO.getCep());
            }

            // Atualiza o endereço do cliente
            clienteExistente.getEndereco().setCep(novoEndereco.getCep());
            clienteExistente.getEndereco().setLogradouro(novoEndereco.getLogradouro());
            clienteExistente.getEndereco().setNumero(novoEndereco.getNumero());
            clienteExistente.getEndereco().setComplemento(novoEndereco.getComplemento());
            clienteExistente.getEndereco().setBairro(novoEndereco.getBairro());
            clienteExistente.getEndereco().setLocalidade(novoEndereco.getLocalidade());
            clienteExistente.getEndereco().setUf(novoEndereco.getUf());
        }

        Cliente clienteAtualizado = clienteRepository.save(clienteExistente);
        log.info("Cliente atualizado com sucesso - ID: {}", id);
        
        return clienteAtualizado;
    }

    public Cliente buscarClientePorCpfCNPJ(String cpfCNPJ) {
        log.debug("Buscando cliente por CPF/CNPJ: {}", cpfCNPJ);
        String cpfCnpjLimpo = cpfCNPJ.replaceAll("\\D", "");
        log.trace("CPF/CNPJ normalizado: {}", cpfCnpjLimpo);
        
        return clienteRepository.findByCpfCNPJ(cpfCnpjLimpo)
                .orElseThrow(() -> {
                    log.error("Cliente não encontrado para CPF/CNPJ: {}", cpfCnpjLimpo);
                    return new ClienteNaoEncontradoException(
                            "Cliente não encontrado com CPF/CNPJ: " + cpfCnpjLimpo
                    );
                });
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