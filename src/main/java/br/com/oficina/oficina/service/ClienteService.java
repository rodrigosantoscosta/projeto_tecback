package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.cliente.CadastrarClienteDTO;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ViaCepService viaCepService;

    public void cadastrarCliente(CadastrarClienteDTO clienteDTO) {
        log.info("Iniciando cadastro de cliente: {}", clienteDTO.getNomeCompleto());
        try {
            // Remove caracteres não numéricos do CPF/CNPJ
            String cpfCNPJ = clienteDTO.getCpfCNPJ().replaceAll("\\D", "");
            log.debug("CPF/CNPJ normalizado: {}", cpfCNPJ);

            validarUnicidade(cpfCNPJ, clienteDTO.getEmail());
            log.debug("Validação de unicidade concluída");

            // Busca endereço via CEP
            var endereco = viaCepService.buscarEConstruirEndereco(
                    clienteDTO.getCep(),
                    clienteDTO.getNumero(),
                    clienteDTO.getComplemento()
            );

            if (endereco == null) {
                log.error("Endereço não encontrado para CEP: {}", clienteDTO.getCep());
                throw new RuntimeException("Erro ao buscar endereço pelo CEP");
            }

            // Cria e salva o cliente
            Cliente cliente = new Cliente();
            cliente.setNomeCompleto(clienteDTO.getNomeCompleto().trim());
            cliente.setCpfCNPJ(cpfCNPJ);
            cliente.setTelefone(clienteDTO.getTelefone().trim());
            cliente.setEmail(clienteDTO.getEmail().trim().toLowerCase());
            cliente.setEndereco(endereco);

            clienteRepository.save(cliente);
            log.info("Cliente cadastrado com sucesso. ID: {}", cliente.getId());

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Violação de integridade de dados", e);
            tratarViolacaoIntegridadeDados(e);
        } catch (jakarta.validation.ConstraintViolationException e) {
            log.error("Violação de restrição de validação", e);
            tratarViolacaoRestricao(e);
        } catch (Exception e) {
            log.error("Erro ao cadastrar cliente", e);
            tratarExcecaoGenerica(e);
        }
    }

    public List<Cliente> listarTodosClientes() {
        log.info("Listando todos os clientes");
        List<Cliente> clientes = clienteRepository.findAll();
        log.debug("Total de clientes encontrados: {}", clientes.size());
        return clientes;
    }

    public Optional<Cliente> buscarClientePorId(UUID id) {
        log.debug("Buscando cliente por ID: {}", id);
        Optional<Cliente> cliente = clienteRepository.findById(id);
        cliente.ifPresentOrElse(
                c -> log.debug("Cliente encontrado: {}", c.getId()),
                () -> log.debug("Cliente não encontrado para ID: {}", id)
        );
        return cliente;
    }

    public Optional<Cliente> buscarClientePorCpf(String cpf) {
        log.debug("Buscando cliente por CPF: {}", cpf);
        String cpfNormalizado = cpf.replaceAll("\\D", "");
        log.trace("CPF normalizado: {}", cpfNormalizado);
        Optional<Cliente> cliente = clienteRepository.findByCpfCNPJ(cpfNormalizado);
        cliente.ifPresentOrElse(
                c -> log.debug("Cliente encontrado: {}", c.getCpfCNPJ()),
                () -> log.debug("Cliente não encontrado para CPF: {}", cpfNormalizado)
        );
        return cliente;
    }

    public void deletarClientePorId(UUID id) {
        clienteRepository.deleteById(id);
    }

    private void validarUnicidade(String cpfCNPJ, String email) throws RuntimeException {
        if (clienteRepository.existsByCpfCNPJ(cpfCNPJ)) {
            throw new RuntimeException("CPF/CNPJ já cadastrado");
        }
        if (clienteRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já cadastrado");
        }
    }

    private void tratarViolacaoIntegridadeDados(org.springframework.dao.DataIntegrityViolationException e) throws RuntimeException {
        if (e.getMessage().contains("cpf_cnpj")) {
            throw new RuntimeException("CPF/CNPJ já está cadastrado no sistema");
        } else if (e.getMessage().contains("email")) {
            throw new RuntimeException("Email já está cadastrado no sistema");
        } else {
            throw new RuntimeException("Erro de integridade dos dados: " + e.getMessage());
        }
    }

    private void tratarViolacaoRestricao(jakarta.validation.ConstraintViolationException e) throws RuntimeException {
        String violations = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .reduce((v1, v2) -> v1 + "; " + v2)
                .orElse("Erro de validação");
        throw new RuntimeException("Dados inválidos: " + violations);
    }

    private void tratarExcecaoGenerica(Exception e) throws RuntimeException {
        throw new RuntimeException("Erro ao cadastrar cliente: " + e.getMessage(), e);
    }
}