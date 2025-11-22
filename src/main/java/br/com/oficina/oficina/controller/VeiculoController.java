package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo veículo",
            description = "Cadastra um novo veículo associado a um cliente existente")
    public ResponseEntity<Veiculo> cadastrarVeiculo(@Valid @RequestBody CadastrarVeiculoDTO dto) {
        log.info("Iniciando cadastro de veículo - Placa: {}", dto.getPlaca());
        Veiculo veiculoSalvo = veiculoService.cadastrarVeiculo(dto);
        log.info("Veículo cadastrado com sucesso - ID: {}, Placa: {}",
                veiculoSalvo.getId(), veiculoSalvo.getPlaca());
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoSalvo);
    }

    @GetMapping
    @Operation(summary = "Listar todos os veículos")
    public ResponseEntity<List<Veiculo>> listarTodos() {
        log.info("Listando todos os veículos");
        List<Veiculo> veiculos = veiculoService.listarTodosVeiculos();
        log.info("Total de veículos encontrados: {}", veiculos.size());
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<Veiculo> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando veículo por ID: {}", id);
        Veiculo veiculo = veiculoService.buscarVeiculoPorId(id);

        return ResponseEntity.ok(veiculo);
    }

    @GetMapping("/placa/{placa}")
    @Operation(summary = "Buscar veículo por placa")
    public ResponseEntity<Veiculo> buscarPorPlaca(@PathVariable String placa) {
        log.info("Buscando veículo por placa: {}", placa);
        Veiculo veiculo = veiculoService.buscarVeiculoPorPlaca(placa);

        return ResponseEntity.ok(veiculo);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar veículos por cliente")
    public ResponseEntity<List<Veiculo>> listarVeiculosPorCliente(@PathVariable UUID clienteId) {
        log.info("Listando veículos do cliente: {}", clienteId);
        List<Veiculo> veiculos = veiculoService.listarVeiculosPorCliente(clienteId);
        log.info("Total de veículos encontrados para o cliente {}: {}", clienteId, veiculos.size());
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/total-veiculos")
    @Operation(summary = "Contar total de veículos")
    public ResponseEntity<Long> contarTotalVeiculos() {
        log.info("Contando total de veículos");
        Long total = veiculoService.contarTotalVeiculos();
        log.info("Total de veículos no sistema: {}", total);
        return ResponseEntity.ok(total);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo")
    public ResponseEntity<Veiculo> atualizar(@PathVariable UUID id,
                                             @Valid @RequestBody CadastrarVeiculoDTO dto) {
        log.info("Atualizando veículo: {}", id);
        Veiculo veiculoAtualizado = veiculoService.atualizar(id, dto);
        log.info("Veículo atualizado com sucesso: {}", id);
        return ResponseEntity.ok(veiculoAtualizado);
    }

    @PutMapping("/{veiculoId}/associar/{clienteId}")
    @Operation(summary = "Associar veículo a cliente")
    public ResponseEntity<Veiculo> associarVeiculoAoCliente(
            @PathVariable UUID veiculoId,
            @PathVariable UUID clienteId) {
        log.info("Associando veículo {} ao cliente {}", veiculoId, clienteId);
        Veiculo veiculo = veiculoService.associarVeiculoAoCliente(veiculoId, clienteId);
        log.info("Veículo associado com sucesso");
        return ResponseEntity.ok(veiculo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar veículo por ID")
    public ResponseEntity<Void> deletarPorId(@PathVariable UUID id) {
        log.info("Iniciando deleção do veículo: {}", id);
        veiculoService.deletarVeiculoPorId(id);
        log.info("Veículo deletado com sucesso: {}", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/placa/{placa}")
    @Operation(summary = "Deletar veículo por placa")
    public ResponseEntity<Void> deletarVeiculoPorPlaca(@PathVariable String placa) {
        log.info("Iniciando deleção do veículo por placa: {}", placa);
        veiculoService.deletarVeiculoPorPlaca(placa);
        log.info("Veículo deletado com sucesso com placa: {}", placa);
        return ResponseEntity.noContent().build();
    }
}