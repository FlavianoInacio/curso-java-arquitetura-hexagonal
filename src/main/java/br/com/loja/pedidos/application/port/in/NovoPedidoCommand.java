package br.com.loja.pedidos.application.port.in;

import br.com.loja.pedidos.domain.model.ItemPedido;
import br.com.loja.pedidos.domain.model.Pedido;

import java.util.List;

public record NovoPedidoCommand(String cliente, List<ItemPedido> itens) {

    public Pedido paraPedido() {
        return Pedido.criar(cliente, itens);
    }
}
