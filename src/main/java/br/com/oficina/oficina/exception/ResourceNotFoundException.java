package br.com.oficina.oficina.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @deprecated Use exceptions mais específicas como ClienteNaoEncontradoException, VeiculoNaoEncontradoException, etc.
 */
@Deprecated(since = "1.0", forRemoval = true)//cool
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
