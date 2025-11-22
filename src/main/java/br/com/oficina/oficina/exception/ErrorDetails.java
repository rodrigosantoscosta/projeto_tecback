package br.com.oficina.oficina.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String mensagem;
    private String path;
    private List<String> detalhes;

    public ErrorDetails(LocalDateTime timestamp, String mensagem, String path) {
        this.timestamp = timestamp;
        this.mensagem = mensagem;
        this.path = path;
    }
}