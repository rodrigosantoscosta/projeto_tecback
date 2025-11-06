package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    public ResponseEntity<String> cadastrarVeiculoCompleto(@RequestBody Map<String, Object> requestMap) {
        try {
            veiculoService.cadastrarVeiculoCompleto(requestMap);
            return ResponseEntity.ok("Veículo cadastrado com sucesso");
        } catch (Exception e) {
            System.out.println("Erro no Controller: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro ao cadastrar veículo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Veiculo>> listarTodos() {
        List<Veiculo> veiculos = veiculoService.listarTodosVeiculos();
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable UUID id) {
        Optional<Veiculo> veiculo = veiculoService.buscarVeiculoPorId(id);

        if (veiculo.isPresent()) {
            return ResponseEntity.ok(veiculo.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Veículo não encontrado");
        }
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<?> buscarPorPlaca(@PathVariable String placa) {
        Optional<Veiculo> veiculo = veiculoService.buscarVeiculoPorPlaca(placa);

        if (veiculo.isPresent()) {
            return ResponseEntity.ok(veiculo.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Veículo não encontrado");
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Veiculo>> listarVeiculosPorCliente(@PathVariable UUID clienteId) {
        List<Veiculo> veiculos = veiculoService.listarVeiculosPorCliente(clienteId);
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/total-veiculos")
    public ResponseEntity<Long> contarTotalVeiculos() {
        Long total = veiculoService.contarTotalVeiculos();
        return ResponseEntity.ok(total);
    }

//    @PutMapping("/{veiculoId}/associar/{clienteId}")
//    public ResponseEntity<String> associarVeiculoAoCliente(
//            @PathVariable UUID veiculoId,
//            @PathVariable UUID clienteId) {
//        try {
//            veiculoService.associarVeiculoAoCliente(veiculoId, clienteId);
//            return ResponseEntity.ok("Veículo associado ao cliente com sucesso");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Erro ao associar veículo: " + e.getMessage());
//        }
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable UUID id) {
        try {
            veiculoService.deletarVeiculoPorId(id);
            return ResponseEntity.ok("Veículo deletado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar veículo: " + e.getMessage());
        }
    }


    @DeleteMapping("/placa/{placa}")
    public ResponseEntity<String> deletarVeiculoPorPlaca(@PathVariable String placa) {
        try {
            veiculoService.deletarVeiculoPorPlaca(placa);
            return ResponseEntity.ok("Veículo deletado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar veículo: " + e.getMessage());
        }
    }
}
