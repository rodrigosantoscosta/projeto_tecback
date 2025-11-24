package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.atendimento.CadastrarAtendimentoDTO;
import br.com.oficina.oficina.dto.veiculo.CadastrarVeiculoDTO;
import br.com.oficina.oficina.model.Atendimento;
import br.com.oficina.oficina.model.Veiculo;
import br.com.oficina.oficina.service.AtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/atendimentos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    public AtendimentoController(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo atendimento",
            description = "Cadastrando um novo atendimento associado a  cliente, veículo e funcionário existentes")
    public ResponseEntity<Atendimento> cadastrarAtendimento(@Valid @RequestBody CadastrarAtendimentoDTO atendimentoDTO) {
        log.info("Iniciando cadastro de Atendimento - Descrição: {}", atendimentoDTO.getDescricaoServico());
        Atendimento atendimentoSalvo = atendimentoService.cadastrarAtendimento(atendimentoDTO);
        log.info("Atendimento Cadastrado com sucesso - ID: {}",
                atendimentoSalvo.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(atendimentoSalvo);

    }
    @GetMapping
    public ResponseEntity<List<Atendimento>> listarTodos() {
        List<Atendimento> atendimentos = atendimentoService.listarTodosAtendimentos();
        return ResponseEntity.ok(atendimentos);
    }

    @PostMapping("atendimentos_completo")
    public ResponseEntity<String> cadastrarAtendimentoSemDados(@RequestBody Atendimento atendimento) {
        try {
            atendimentoService.cadastrarAtendimentoSemDados(atendimento);
            return ResponseEntity.ok("Atendimento cadastrado com sucesso");
        } catch (Exception e) {
            System.out.println("Erro no Controller: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro ao cadastrar atendimento: " + e.getMessage());
        }

    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Buscar Atendimento por ID")
    public ResponseEntity<?> BuscarAtendimentoporId(@PathVariable UUID id) {
        log.info("Buscando Atendimento por ID: {}", id);
       Atendimento atendimento = atendimentoService.buscarAtendimentoPorId(id);

       return  ResponseEntity.ok(atendimento);

    }

    @PutMapping("/atualizar/{id}")
    @Operation(summary = "Atualizar Atendimento por ID")
    public ResponseEntity<Atendimento> atualizar(@PathVariable UUID id,
                                             @Valid @RequestBody CadastrarAtendimentoDTO atendimentoDTO) {
        log.info("Atualizando atendimento: {}", id);
        Atendimento atendimentoAtualizado = atendimentoService.atualizarAtendimento(id, atendimentoDTO);
        log.info("Atendimento atualizado com sucesso: {}", id);
        return ResponseEntity.ok(atendimentoAtualizado);
    }

    @GetMapping("/Cliente ID/{clienteId}")
    @Operation(summary = "Listar Atendimentos por Cliente ID")
    public ResponseEntity<List<Atendimento>> listarAtendimentoporClienteCpfCNPJ(@PathVariable UUID clienteId) {
        log.info("Listando Atendimentos do Cliente por ID: {}", clienteId);
        List<Atendimento> atendimentos = atendimentoService.listarAtendimentosPorClienteID(clienteId);
        return ResponseEntity.ok(atendimentos);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletarAtendimentoporId(@PathVariable UUID id) {
        try {
            atendimentoService.deletarAtendimentoPorId(id);
            return ResponseEntity.ok("Atendimento deletado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar Atendimento: " + e.getMessage());
        }
    }
}

