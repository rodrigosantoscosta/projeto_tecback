package br.com.oficina.oficina.controller;

import br.com.oficina.oficina.dto.funcionario.CadastrarFuncionarioDTO;
import br.com.oficina.oficina.dto.funcionario.FuncionarioDTO;
import br.com.oficina.oficina.dto.auth.AuthenticationRequest;
import br.com.oficina.oficina.dto.auth.AuthenticationResponse;
import br.com.oficina.oficina.mapper.FuncionarioMapper;
import br.com.oficina.oficina.model.Funcionario;
import br.com.oficina.oficina.service.FuncionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/funcionarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Funcionarios", description = "Endpoints para gerenciamento de funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final FuncionarioMapper funcionarioMapper;
    private final AuthenticationManager authenticationManager;
    private final br.com.oficina.oficina.security.JwtUtil jwtUtil;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<FuncionarioDTO> criarFuncionario(@Valid @RequestBody CadastrarFuncionarioDTO dto) {
        Funcionario funcionario = funcionarioService.cadastrarFuncionario(dto);
        return ResponseEntity.ok(funcionarioMapper.toDTO(funcionario));
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

    // retorna o funcionario autenticado
    @GetMapping("/me")
    public ResponseEntity<FuncionarioDTO> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            return ResponseEntity.status(401).build();
        }

        return funcionarioService.buscarPorUsuario(username)
            .map(f -> ResponseEntity.ok(funcionarioMapper.toDTO(f)))
            .orElse(ResponseEntity.notFound().build());
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
        Funcionario funcionario = funcionarioService.buscarPorId(id);
        FuncionarioDTO dto = funcionarioMapper.toDTO(funcionario);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Funcionario")
    public ResponseEntity<Void> deletarPorId(@PathVariable UUID id) {
        log.info("Iniciando deleção do funcionario: {}", id);
        funcionarioService.deletarFuncionarioPorId(id);
        log.info("Funcionario deletado com sucesso: {}", id);

        return ResponseEntity.noContent().build();
    }
}
