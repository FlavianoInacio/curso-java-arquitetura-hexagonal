package br.com.loja.pedidos.adapter.in.web;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import br.com.loja.pedidos.domain.exception.PedidoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PedidoInvalidoException.class)
    public ProblemDetail pedidoInvalido(PedidoInvalidoException e){
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Pedido Inválido");
        return problem;
    }

    @ExceptionHandler(PedidoNaoEncontradoException.class)
    public ProblemDetail pedidoNaoEncontrado(PedidoNaoEncontradoException e){
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Pedido não encontrado!");
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail requisicaoInvalida(HttpMessageNotReadableException e){
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "O corpo da requisição é inválido");
        problem.setTitle("Requisição inválida");
        return problem;
    }


}
