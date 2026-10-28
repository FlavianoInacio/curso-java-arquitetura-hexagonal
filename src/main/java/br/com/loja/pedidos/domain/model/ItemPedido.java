package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;

import java.math.BigDecimal;

public record ItemPedido(String produto, int quantidade, BigDecimal precoUnitario) {

    public ItemPedido {
        if (produto == null || produto.isBlank()) {
            throw new PedidoInvalidoException("O produto do item é obrigatório!");
        }
        if (quantidade <= 0) {
            throw new PedidoInvalidoException("A quantidade deve ser maior que zero");
        }

        if (precoUnitario == null || precoUnitario.signum() <= 0) {
            throw new PedidoInvalidoException("O preço unitário deve ser maior que zero");
        }
    }

    public BigDecimal subTotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

}
