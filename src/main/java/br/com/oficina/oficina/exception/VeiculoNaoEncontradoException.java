package br.com.oficina.oficina.exception;

public class VeiculoNaoEncontradoException extends RuntimeException {
    public VeiculoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}