package br.com.oficina.oficina.exception;

public class AtendimentoNaoEncontrado extends RuntimeException {
    public AtendimentoNaoEncontrado(String message) {
        super(message);
    }
}
