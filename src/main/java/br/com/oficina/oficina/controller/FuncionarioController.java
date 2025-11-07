package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.mapper.FuncionarioMapper;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/funcionarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final FuncionarioMapper funcionarioMapper;


    @PostMapping
    public ResponseEntity<FuncionarioDTO> criarFuncionario(@Valid @RequestBody CadastrarFuncionarioDTO dto) {
        Funcionario funcionario = funcionarioService.cadastrarFuncionario(dto);
        return ResponseEntity.ok(funcionarioMapper.toDTO(funcionario));
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioDTO>> listarTodosFuncionarios() {
        var funcionarios = funcionarioService.listarTodosFuncionarios().stream()
            .map(funcionarioMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> buscarFuncionarioPorId(@PathVariable UUID id) {
        return funcionarioService.buscarPorId(id)
            .map(funcionario -> ResponseEntity.ok(funcionarioMapper.toDTO(funcionario)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credenciais) {
        String usuario = credenciais.get("usuario");
        String senha = credenciais.get("senha");

        boolean autenticado = funcionarioService.autenticar(usuario, senha);
        if (!autenticado) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }

        return ResponseEntity.ok("Login realizado com sucesso");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarFuncionarioPorId(@PathVariable UUID id) {
        try {
            funcionarioService.deletarFuncionarioPorId(id);
            return ResponseEntity.ok("Funcionario deletado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao deletar funcionario: " + e.getMessage());
        }
    }
}
