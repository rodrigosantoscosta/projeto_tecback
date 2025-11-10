package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.dto.auth.AuthenticationRequest;
import br.com.oficina.oficina.dto.auth.AuthenticationResponse;
import br.com.oficina.oficina.mapper.FuncionarioMapper;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.security.JwtUtil;
import br.com.oficina.oficina.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

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
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


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
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest req) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(req.getUsuario(), req.getSenha());
            var auth = authenticationManager.authenticate(authToken);
            var userDetails = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
            var token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthenticationResponse("Bearer", token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
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
