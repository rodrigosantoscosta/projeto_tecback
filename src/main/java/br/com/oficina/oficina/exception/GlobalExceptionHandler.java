package br.com.oficina.oficina.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<ErrorDetails> handleCepNaoEncontrado(
            CepNaoEncontradoException e,
            HttpServletRequest request) {

        log.error("CEP não encontrado: {}", e.getMessage());

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErrorDetails> handleClienteNaoEncontrado(
            ClienteNaoEncontradoException e,
            HttpServletRequest request) {

        log.error("Cliente não encontrado: {}", e.getMessage());

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(VeiculoNaoEncontradoException.class)
    public ResponseEntity<ErrorDetails> handleVeiculoNaoEncontrado(
            VeiculoNaoEncontradoException e,
            HttpServletRequest request) {

        log.error("Veículo não encontrado: {}", e.getMessage());

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ClienteComVeiculosException.class)
    public ResponseEntity<ErrorDetails> handleClienteComVeiculos(
            ClienteComVeiculosException e,
            HttpServletRequest request) {

        log.warn("Operação não permitida: {}", e.getMessage());

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RecursoJaCadastradoException.class)
    public ResponseEntity<ErrorDetails> handleRecursoJaCadastrado(
            RecursoJaCadastradoException e,
            HttpServletRequest request) {

        log.warn("Recurso já cadastrado: {}", e.getMessage());

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        log.error("Erro de validação: {}", e.getMessage());

        List<String> erros = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.toList());

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "Erro de validação dos dados",
                request.getRequestURI(),
                erros
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDetails> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request) {

        log.error("Erro em tempo de execução: {}", e.getMessage(), e);

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleException(
            Exception e,
            HttpServletRequest request) {

        log.error("Erro inesperado: {}", e.getMessage(), e);

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "Erro interno do servidor",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}