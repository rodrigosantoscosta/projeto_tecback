package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.dto.veiculo.VeiculoDTO;
import br.com.oficina.oficina.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    public ResponseEntity<VeiculoDTO> cadastrarVeiculo(@Valid @RequestBody CadastrarVeiculoDTO dto) {
        log.info("Iniciando cadastro de veículo - Placa: {}", dto.getPlaca());
        VeiculoDTO veiculoSalvo = veiculoService.cadastrarVeiculo(dto);
        log.info("Veículo cadastrado com sucesso - ID: {}, Placa: {}",
                veiculoSalvo.getId(), veiculoSalvo.getPlaca());
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoSalvo);
    }

    @GetMapping
    @Operation(summary = "Listar todos os veículos")
    public ResponseEntity<List<VeiculoDTO>> listarTodos() {
        log.info("Listando todos os veículos");
        List<VeiculoDTO> veiculos = veiculoService.listarTodosVeiculos();
        log.info("Total de veículos encontrados: {}", veiculos.size());
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<VeiculoDTO> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando veículo por ID: {}", id);
        VeiculoDTO veiculo = veiculoService.buscarVeiculoPorId(id);
        return ResponseEntity.ok(veiculo);
    }

    @GetMapping("/placa/{placa}")
    @Operation(summary = "Buscar veículo por placa")
    public ResponseEntity<VeiculoDTO> buscarPorPlaca(@PathVariable String placa) {
        log.info("Buscando veículo por placa: {}", placa);
        VeiculoDTO veiculo = veiculoService.buscarVeiculoPorPlaca(placa);
        return ResponseEntity.ok(veiculo);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar veículos por cliente")
    public ResponseEntity<List<VeiculoDTO>> listarVeiculosPorCliente(@PathVariable UUID clienteId) {
        log.info("Listando veículos do cliente: {}", clienteId);
        List<VeiculoDTO> veiculos = veiculoService.listarVeiculosPorCliente(clienteId);
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
    public ResponseEntity<VeiculoDTO> atualizar(@PathVariable UUID id,
                                                @Valid @RequestBody CadastrarVeiculoDTO dto) {
        log.info("Atualizando veículo: {}", id);
        VeiculoDTO veiculoAtualizado = veiculoService.atualizarVeiculo(id, dto);
        log.info("Veículo atualizado com sucesso: {}", id);
        return ResponseEntity.ok(veiculoAtualizado);
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
