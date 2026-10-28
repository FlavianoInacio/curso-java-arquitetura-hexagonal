package br.com.loja.pedidos.domain.exception;

public class PedidoInvalidoException extends RuntimeException{

    public PedidoInvalidoException(String mensagem){
        super(mensagem);
    }
}
