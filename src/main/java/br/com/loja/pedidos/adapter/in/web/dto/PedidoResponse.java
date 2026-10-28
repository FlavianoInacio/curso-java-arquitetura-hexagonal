package br.com.loja.pedidos.adapter.in.web.dto;

import br.com.loja.pedidos.domain.model.Pedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        String cliente,
        String status,
        List<ItemResponse> itens,
        BigDecimal total,
        Instant criadoEm) {

    public record ItemResponse(String produto, int quantidade,
                               BigDecimal precoUnitario, BigDecimal subtotal) {
    }

    public static PedidoResponse de(Pedido pedido) {
        var itens = pedido.getItens().stream()
                .map(i -> new ItemResponse(i.produto(), i.quantidade(), i.precoUnitario(), i.subTotal()))
                .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCliente(),
                pedido.getStatus().name(),
                itens,
                pedido.total(),
                pedido.getCriadoEm());
    }
}