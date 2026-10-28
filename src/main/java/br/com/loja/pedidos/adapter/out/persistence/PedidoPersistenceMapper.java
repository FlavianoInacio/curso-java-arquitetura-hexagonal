package br.com.loja.pedidos.adapter.out.persistence;

import br.com.loja.pedidos.application.port.out.ItemPedidoEmbeddable;
import br.com.loja.pedidos.application.port.out.PedidoEntity;
import br.com.loja.pedidos.domain.model.ItemPedido;
import br.com.loja.pedidos.domain.model.Pedido;

final public class PedidoPersistenceMapper {

    private PedidoPersistenceMapper() {

    }

    static PedidoEntity paraEntity(Pedido pedido) {

        var itens = pedido.getItens()
                .stream()
                .map(item -> new ItemPedidoEmbeddable(item.produto(), item.quantidade(), item.precoUnitario()))
                .toList();

        return new PedidoEntity(pedido.getId(), pedido.getCliente(), pedido.getStatus(), pedido.getCriadoEm(), itens);
    }

    static Pedido paraDominio(PedidoEntity pedido) {

        var itens = pedido.getItens()
                .stream()
                .map(item -> new ItemPedido(item.getProduto(), item.getQuantidade(), item.getPrecoUnitario()))
                .toList();

        return Pedido.reconstruir(pedido.getId(), pedido.getCliente(), itens, pedido.getStatus(), pedido.getCriadoEm());
    }


}
