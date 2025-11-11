package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.cliente.ClienteDTO;
import br.com.oficina.oficina.dto.cliente.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<String> cadastrarCliente(@Valid @RequestBody CadastrarVeiculoDTO dto) {
        log.info("Iniciando cadastro de cliente");
        try {
            clienteService.cadastrarCliente(dto);
            log.info("Cliente cadastrado com sucesso");
            return ResponseEntity.ok("Cliente cadastrado com sucesso");
        } catch (RuntimeException e) {
            log.error("Erro ao cadastrar cliente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro interno ao cadastrar cliente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarTodosClientes() {
        log.info("Listando todos os clientes");
        List<ClienteDTO> clientes = clienteService.listarTodosClientes().stream()
                .map(this::toDTO) // ← Método privado no controller
                .collect(Collectors.toList());
        log.debug("Total de clientes encontrados: {}", clientes.size());
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable UUID id) {
        log.info("Buscando cliente por ID: {}", id);
        Optional<Cliente> cliente = clienteService.buscarClientePorId(id);

        if (cliente.isPresent()) {
            log.debug("Cliente encontrado: {}", cliente.get().getId());
            return ResponseEntity.ok(toDTO(cliente.get()));
        } else {
            log.warn("Cliente não encontrado para ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Cliente não encontrado");
        }
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<?> buscarClientePorCpf(@PathVariable String cpf) {
        log.info("Buscando cliente por CPF: {}", cpf);
        Optional<Cliente> cliente = clienteService.buscarClientePorCpf(cpf);

        if (cliente.isPresent()) {
            log.debug("Cliente encontrado: {}", cliente.get().getCpfCNPJ());
            return ResponseEntity.ok(toDTO(cliente.get()));
        } else {
            log.warn("Cliente não encontrado para CPF: {}", cpf);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Cliente não encontrado");
        }
    }

    //  Entity -> DTO
    private ClienteDTO toDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNomeCompleto(),
                cliente.getCpfCNPJ(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getDataCadastro()
        );
    }
}