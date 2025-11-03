package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "*") // Permite qualquer origem para desenvolvimento
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<String> cadastrarClienteCompleto(@RequestBody Map<String, Object> requestMap) {
        try {
            clienteService.cadastrarClienteCompleto(requestMap);
            return ResponseEntity.ok("Cliente cadastrado com sucesso");
        } catch (Exception e) {
            System.out.println("Erro no Controller: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.listarTodosClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        Optional<Cliente> cliente = clienteService.buscarClientePorId(id);

        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Cliente não encontrado" );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable UUID id) {
        try {

            clienteService.deletarClientePorId(id);
            return ResponseEntity.ok("Cliente deletado com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar cliente: " + e.getMessage());
        }
    }
}