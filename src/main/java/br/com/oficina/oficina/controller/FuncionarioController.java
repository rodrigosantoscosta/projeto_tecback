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
    @Operation(summary = "Cadastrar funcionário")
    public ResponseEntity<FuncionarioDTO> criarFuncionario(@Valid @RequestBody CadastrarFuncionarioDTO dto) {
        log.info("Iniciando o cadastro de funcionário");
        Funcionario funcionario = funcionarioService.cadastrarFuncionario(dto);
        log.info("Funcionário cadastrado com sucesso");
        return ResponseEntity.ok(funcionarioMapper.toDTO(funcionario));
    }

    @PostMapping("/login")
    @Operation(summary = "Login de funcionário")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest req) {
        log.info("Login do funcionário");
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
    @Operation(summary = "Funcionário logado")
    public ResponseEntity<FuncionarioDTO> me() {
        log.info("Funcionário logado");
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
    @Operation(summary = "Listando todos os funcionários")
    public ResponseEntity<List<FuncionarioDTO>> listarTodosFuncionarios() {
        log.info("Listando todos os funcionários");
        List<FuncionarioDTO> funcionarios = funcionarioService.listarTodosFuncionarios().stream()
                .map(FuncionarioDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscando funcionário")
    public ResponseEntity<?> buscarFuncionarioPorId(@PathVariable UUID id) {
        log.info("Iniciando busca do funcionario: {}", id);
        FuncionarioDTO funcionarioDTO = funcionarioService.buscarPorId(id);
        log.info("Funcionario encontrado com sucesso: {}", id);
        return ResponseEntity.ok(funcionarioDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar Funcionario")
    public ResponseEntity<Void> deletarPorId(@PathVariable UUID id) {
        log.info("Iniciando deleção do funcionario: {}", id);
        funcionarioService.deletarFuncionarioPorId(id);
        log.info("Funcionario deletado com sucesso: {}", id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar funcionário")
    public ResponseEntity<String> atualizarFuncionario(
            @PathVariable UUID id,
            @Valid @RequestBody CadastrarFuncionarioDTO dto
    ){
        log.info("Iniciando atualização de funcionário com ID: {}", id);
        funcionarioService.atualizarFuncionario(id, dto);
        log.info("funcionário com ID {} atualizado com sucesso", id);
        return ResponseEntity.ok("Funcionário atualizado com sucesso");
    }
}
