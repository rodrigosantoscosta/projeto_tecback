package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<String> cadastrarCliente(@RequestBody Map<String, Object> requestMap) {

        clienteService.cadastrarClienteCompleto(
                (String) requestMap.get("nome"),
                (String) requestMap.get("cpf"),
                (String) requestMap.get("telefone"),
                (String) requestMap.get("email"),
                (String) requestMap.get("cep"),
                (String) requestMap.get("numero"),
                (String) requestMap.get("complemento")
        );
        return ResponseEntity.ok("Cliente cadastrado com sucesso!");
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.listarTodosClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Cliente>> buscarPorId(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable Long id) {
        clienteService.deletarClientePorId(id);
        return ResponseEntity.ok("Cliente deletado com sucesso!");
    }
}