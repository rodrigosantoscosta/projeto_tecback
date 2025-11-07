package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.cliente.ClienteDTO;
import br.com.oficina.oficina.dto.cliente.CriarClienteDTO;
import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<String> cadastrarCliente(@Valid @RequestBody CriarClienteDTO dto) {
        try {
            clienteService.cadastrarCliente(dto);
            return ResponseEntity.ok("Cliente cadastrado com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarTodosClientes() {
        List<ClienteDTO> clientes = clienteService.listarTodosClientes().stream()
                .map(this::toDTO) // ← Método privado no controller
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarClientePorId(@PathVariable UUID id) {
        Optional<Cliente> cliente = clienteService.buscarClientePorId(id);

        if (cliente.isPresent()) {
            return ResponseEntity.ok(toDTO(cliente.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Cliente não encontrado");
        }
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<?> buscarClientePorCpf(@PathVariable String cpf) {
        Optional<Cliente> cliente = clienteService.buscarClientePorCpf(cpf);

        if (cliente.isPresent()) {
            return ResponseEntity.ok(toDTO(cliente.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Cliente não encontrado");
        }
    }

    // Converte Entity -> DTO
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