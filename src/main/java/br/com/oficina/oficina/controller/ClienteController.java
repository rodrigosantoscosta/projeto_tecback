package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.model.Cliente;
import br.com.oficina.oficina.service.ClienteService;
import br.com.oficina.oficina.dto.CadastrarClienteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo cliente")
    public ResponseEntity<String> cadastrarCliente(@Valid @RequestBody CadastrarClienteDTO dto) {
        log.info("Iniciando cadastro de cliente");
        clienteService.cadastrarCliente(dto);
        log.info("Cliente cadastrado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body("Cliente cadastrado com sucesso");
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes")
    public ResponseEntity<List<Cliente>> listarTodos() {
        log.info("Listando todos os clientes");
        List<Cliente> clientes = clienteService.listarTodosClientes();
        log.info("Total de clientes encontrados: {}", clientes.size());
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando cliente por ID: {}", id);
        Cliente cliente = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/cpf/{cpfCNPJ}")
    @Operation(summary = "Buscar cliente por CPF/CNPJ")
    public ResponseEntity<Cliente> buscarPorCPFouCNPJ(@PathVariable String cpfCNPJ) {
        log.info("Buscando cliente por CPF/CNPJ: {}", cpfCNPJ);

        String cpfCnpjLimpo = cpfCNPJ.replaceAll("\\D", "");
        log.debug("CPF/CNPJ normalizado: {}", cpfCnpjLimpo);

        Optional<Cliente> cliente = clienteService.buscarClientePorCpfCNPJ(cpfCnpjLimpo);

        if (cliente.isPresent()) {
            log.info("Cliente encontrado: {}", cliente.get().getNomeCompleto());
            return ResponseEntity.ok(cliente.get());
        } else {
            log.warn("Cliente não encontrado para CPF/CNPJ: {}", cpfCnpjLimpo);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cliente")
    public ResponseEntity<Void> deletarPorId(@PathVariable UUID id) {
        log.info("Iniciando deleção do cliente: {}", id);
        clienteService.deletarClientePorId(id);
        log.info("Cliente deletado com sucesso: {}", id);
        return ResponseEntity.noContent().build();
    }
}