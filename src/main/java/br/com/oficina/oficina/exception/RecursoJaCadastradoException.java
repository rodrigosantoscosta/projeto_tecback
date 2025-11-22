package br.com.oficina.oficina.exception;

public class RecursoJaCadastradoException extends RuntimeException {
    public RecursoJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}