package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.atendimento.AtendimentoDTO;
import br.com.oficina.oficina.dto.atendimento.CadastrarAtendimentoDTO;
import br.com.oficina.oficina.service.AtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/atendimentos")
@Tag(name ="Atendimentos", description = "Endpoints para gerenciamento de Atendimentos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    public AtendimentoController(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    @PostMapping("/cadastrar")
    @Operation(summary = "Cadastrar novo atendimento",
            description = "Cadastrando um novo atendimento associado a  cliente, veículo e funcionário existentes")
    public ResponseEntity<AtendimentoDTO> cadastrarAtendimento(@Valid @RequestBody CadastrarAtendimentoDTO atendimentoDTO) {
        log.info("Iniciando cadastro de Atendimento - Descrição: {}", atendimentoDTO.getDescricaoServico());
        AtendimentoDTO atendimentoSalvo = atendimentoService.cadastrarAtendimento(atendimentoDTO);
        log.info("Atendimento Cadastrado com sucesso - ID: {}",
                atendimentoSalvo.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(atendimentoSalvo);

    }
    @GetMapping("/listar-todos")
    @Operation(summary = "Listar todos os Atendimentos",
    description = "Retorna uma lista de todos os atendimentos cadastrados no sistema")
    public ResponseEntity<List<AtendimentoDTO>> listarTodos() {
        log.info("Listando todos os Atendimentos");
        List<AtendimentoDTO> atendimentos = atendimentoService.listarTodosAtendimentos();
        log.error("Total de Atendimentos encontrados: {}", atendimentos.size());
        return ResponseEntity.ok(atendimentos);
    }
    @GetMapping("/listar-concluidos")
    @Operation(summary = "Listar Atendimentos Concluídos",
    description = "Retorna uma lista de todos os atendimentos com status CONCLUIDO")
    public ResponseEntity<List<AtendimentoDTO>> listarAtendimentosConcluidos() {
        log.info("Listando Atendimentos Concluídos");
        List<AtendimentoDTO> atendimentoConluidos = atendimentoService.listarAtendimentosConcluidos();
        log.info("Total de Atendimentos Concluídos encontrados: {}", atendimentoConluidos.size());
        return ResponseEntity.ok(atendimentoConluidos);
    }
    @GetMapping("/listar-ordem-decrescente")
    @Operation(summary = "Listar Atendimentos Ordenados por Data de Entrada Decrescente",
    description = "Retorna uma lista de todos os atendimentos ordenados por data de entrada em ordem decrescente")
    public ResponseEntity<List<AtendimentoDTO>> ordenarAtendimentosporDataEntrada(){
        log.info("Listando Atendimentos Ordenados por Data de Entrada Decrescente");
        List<AtendimentoDTO> atendimentosOrdenados = atendimentoService.ordenarAtendimentosporDataEntrada();
        log.info("Total de Atendimentos encontrados: {}", atendimentosOrdenados.size());
        return ResponseEntity.ok(atendimentosOrdenados);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Buscar Atendimento por ID",
    description = "Busca um atendimento específico pelo seu ID único")
    public ResponseEntity<?> BuscarAtendimentoporId(@PathVariable UUID id) {
        log.info("Buscando Atendimento por ID: {}", id);
       AtendimentoDTO atendimento = atendimentoService.buscarAtendimentoPorId(id);

       return  ResponseEntity.ok(atendimento);

    }

    @PutMapping("/atualizar/{id}")
    @Operation(summary = "Atualizar Atendimento por ID",
            description = "Atualiza os detalhes de um atendimento existente pelo seu ID")
    public ResponseEntity<AtendimentoDTO> atualizar(@PathVariable UUID id,
                                                    @Valid @RequestBody CadastrarAtendimentoDTO atendimentoDTO) {
        log.info("Atualizando atendimento: {}", id);
        AtendimentoDTO atendimentoAtualizado = atendimentoService.atualizarAtendimento(id, atendimentoDTO);
        log.info("Atendimento atualizado com sucesso: {}", id);
        return ResponseEntity.ok(atendimentoAtualizado);
    }

    @GetMapping("/cliente ID/{clienteId}")
    @Operation(summary = "Listar Atendimentos por Cliente ID",
    description = "Busca atendimentos associados a um cliente específico pelo ID do cliente")
    public ResponseEntity<List<AtendimentoDTO>> listarAtendimentoporClienteCpfCNPJ(@PathVariable UUID clienteId) {
        log.info("Listando Atendimentos do Cliente por ID: {}", clienteId);
        List<AtendimentoDTO> atendimentos = atendimentoService.listarAtendimentosPorClienteID(clienteId);
        return ResponseEntity.ok(atendimentos);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Deletar Atendimento por ID",
    description = "Deleta um atendimento específico pelo seu ID único")
    public ResponseEntity<String> deletarAtendimentoporId(@PathVariable UUID id) {
        try {
            atendimentoService.deletarAtendimentoPorId(id);
            return ResponseEntity.ok("Atendimento deletado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar Atendimento: " + e.getMessage());
        }
    }
}

