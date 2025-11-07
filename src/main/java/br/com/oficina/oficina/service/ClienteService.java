package br.com.oficina.oficina.service;

import br.com.oficina.oficina.dto.cliente.CriarClienteDTO;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ViaCepService viaCepService;

    public void cadastrarCliente(CriarClienteDTO clienteDTO) {
        try {
            // Remove caracteres não numéricos do CPF/CNPJ
            String cpfCNPJ = clienteDTO.getCpfCNPJ().replaceAll("\\D", "");

            validarUnicidade(cpfCNPJ, clienteDTO.getEmail());

            // Busca endereço via CEP
            var endereco = viaCepService.buscarEConstruirEndereco(
                    clienteDTO.getCep(),
                    clienteDTO.getNumero(),
                    clienteDTO.getComplemento()
            );

            if (endereco == null) {
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

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            tratarViolacaoIntegridadeDados(e);
        } catch (jakarta.validation.ConstraintViolationException e) {
            tratarViolacaoRestricao(e);
        } catch (Exception e) {
            tratarExcecaoGenerica(e);
        }
    }

    public List<Cliente> listarTodosClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarClientePorId(UUID id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarClientePorCpf(String cpf) {
        // Normaliza o CPF removendo caracteres não numéricos
        String cpfNormalizado = cpf.replaceAll("\\D", "");
        return clienteRepository.findByCpfCNPJ(cpfNormalizado);
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