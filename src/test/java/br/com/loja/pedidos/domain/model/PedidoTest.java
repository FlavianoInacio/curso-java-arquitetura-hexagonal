package br.com.loja.pedidos.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoTest {

    private final ItemPedido teclado = new ItemPedido("Teclado", 2, new BigDecimal("150.00"));
    private final ItemPedido mouse = new ItemPedido("Mouse", 1, new BigDecimal("80.00"));

    @Test
    void deveCriarPedidoComStatusCriado(){
        var pedido = Pedido.criar("Maria", List.of(teclado));

        assertThat(pedido.getId()).isNotNull();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.getCriadoEm()).isNotNull();
    }


}