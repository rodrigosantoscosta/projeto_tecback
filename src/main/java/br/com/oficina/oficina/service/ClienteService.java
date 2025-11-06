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
        try {
            // System.out.println("=== INICIANDO CADASTRO DE CLIENTE ===");
            // System.out.println("Dados recebidos: " + requestMap);
            
            //Ideal seria utilizar um metodo DTO, mas por praticidade este metodo pega o JSON e converte para String usando
            // casting (String)
            String nomeCompleto = (String) requestMap.get("nomeCompleto");
            String cpfCNPJ = (String) requestMap.get("cpfCNPJ");
            String telefone = (String) requestMap.get("telefone");
            String email = (String) requestMap.get("email");
            String cep = (String) requestMap.get("cep");
            String numero = (String) requestMap.get("numero");
            String complemento = (String) requestMap.get("complemento");
            
            // System.out.println("Dados extraídos:");
            // System.out.println("- Nome: " + nomeCompleto);
            // System.out.println("- CPF/CNPJ: " + cpfCNPJ);
            // System.out.println("- Telefone: " + telefone);
            // System.out.println("- Email: " + email);
            // System.out.println("- CEP: " + cep);
            // System.out.println("- Número: " + numero);
            // System.out.println("- Complemento: " + complemento);

            // Validações básicas
//            if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
//                throw new RuntimeException("Nome completo é obrigatório");
//            }
//            if (cpfCNPJ == null || cpfCNPJ.trim().isEmpty()) {
//                throw new RuntimeException("CPF/CNPJ é obrigatório");
//            }
//            if (email == null || email.trim().isEmpty()) {
//                throw new RuntimeException("Email é obrigatório");
//            }
//            if (telefone == null || telefone.trim().isEmpty()) {
//                throw new RuntimeException("Telefone é obrigatório");
//            }
//            if (cep == null || cep.trim().isEmpty()) {
//                throw new RuntimeException("CEP é obrigatório");
//            }
//            if (numero == null || numero.trim().isEmpty()) {
//                throw new RuntimeException("Número é obrigatório");
//            }

            // Remove caracteres não numéricos
            cpfCNPJ = cpfCNPJ.replaceAll("\\D", "");
            
            // Verifica se CPF/CNPJ já existe
            if (clienteRepository.existsByCpfCNPJ(cpfCNPJ)) {
                throw new RuntimeException("CPF/CNPJ já cadastrado");
            }
            
            // Verifica se email já existe
            if (clienteRepository.existsByEmail(email)) {
                throw new RuntimeException("Email já cadastrado");
            }

            // Busca endereço via CEP
            var endereco = viaCepService.buscarEConstruirEndereco(cep, numero, complemento);
            
            if (endereco == null) {
                throw new RuntimeException("Erro ao buscar endereço pelo CEP");
            }

            // Cria e salva o cliente
            Cliente cliente = new Cliente();
            cliente.setNomeCompleto(nomeCompleto.trim());
            cliente.setCpfCNPJ(cpfCNPJ);
            cliente.setTelefone(telefone.trim());
            cliente.setEmail(email.trim().toLowerCase());
            cliente.setEndereco(endereco);

            clienteRepository.save(cliente);
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // System.out.println("=== ERRO DE INTEGRIDADE ===");
            // System.out.println("Mensagem: " + e.getMessage());
            // System.out.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "Nenhuma"));
            
            if (e.getMessage().contains("cpf_cnpj")) {
                throw new RuntimeException("CPF/CNPJ já está cadastrado no sistema");
            } else if (e.getMessage().contains("email")) {
                throw new RuntimeException("Email já está cadastrado no sistema");
            } else {
                throw new RuntimeException("Erro de integridade dos dados: " + e.getMessage());
            }
        } catch (jakarta.validation.ConstraintViolationException e) {
            // System.out.println("=== ERRO DE VALIDAÇÃO ===");
            String violations = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .reduce((v1, v2) -> v1 + "; " + v2)
                .orElse("Erro de validação");
            // System.out.println("Violações: " + violations);
            throw new RuntimeException("Dados inválidos: " + violations);
        } catch (org.springframework.transaction.TransactionSystemException e) {
            // System.out.println("=== ERRO DE TRANSAÇÃO ===");
            // System.out.println("Mensagem: " + e.getMessage());
            // System.out.println("Causa raiz: " + e.getRootCause());
            // if (e.getRootCause() != null) {
            //     e.getRootCause().printStackTrace();
            // }
            throw new RuntimeException("Erro de transação: " + (e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage()));
        } catch (Exception e) {
            // System.out.println("=== ERRO GENÉRICO ===");
            // System.out.println("Tipo: " + e.getClass().getSimpleName());
            // System.out.println("Mensagem: " + e.getMessage());
            // System.out.println("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "Nenhuma"));
            // e.printStackTrace();
            throw new RuntimeException("Erro ao cadastrar cliente: " + e.getMessage(), e);
        }
    }
}
