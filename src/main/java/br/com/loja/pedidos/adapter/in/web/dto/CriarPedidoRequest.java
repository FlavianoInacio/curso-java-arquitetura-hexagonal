package br.com.loja.pedidos.adapter.in.web.dto;

import br.com.loja.pedidos.application.port.in.NovoPedidoCommand;
import br.com.loja.pedidos.domain.model.ItemPedido;

import java.math.BigDecimal;
import java.util.List;

public record CriarPedidoRequest(String cliente, List<ItemRequest> itens) {

    public record ItemRequest(String produto, int quantidade, BigDecimal precoUnitario) {
    }

    public NovoPedidoCommand paraCommand() {
        List<ItemPedido> itensDominio = itens == null ? List.of() : itens.stream()
                .map(i -> new ItemPedido(i.produto(), i.quantidade(), i.precoUnitario()))
                .toList();
        return new NovoPedidoCommand(cliente, itensDominio);
    }
}