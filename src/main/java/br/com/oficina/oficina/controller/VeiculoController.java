package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    public ResponseEntity<String> cadastrarVeiculo(@Valid @RequestBody CadastrarVeiculoDTO veiculoDTO) {
        try {
            veiculoService.cadastrarVeiculo(veiculoDTO);
            return ResponseEntity.ok("Veículo cadastrado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar veículo: " + e.getMessage());
        }
    }

    @GetMapping("/veiculos")
    public ResponseEntity<Long> contarTotalVeiculos() {
        Long total = veiculoService.contarTotalVeiculos();
        return ResponseEntity.ok(total);
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
