package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPedidoTest {

    @Test
    void deveCalcularSubtotal() {
        // Arrange
        var item = new ItemPedido("Teclado", 2, new BigDecimal("150.00"));

        // Act
        var subtotal = item.subTotal();

        // Assert
        assertEquals(new BigDecimal("300.00"), subtotal);
    }

    @Test
    void naoDeveAceitarQuantidadeZero() {
        var erro = assertThrows(PedidoInvalidoException.class,
                () -> new ItemPedido("Teclado", 0, BigDecimal.TEN));
        assertEquals("A quantidade deve ser maior que zero", erro.getMessage());
    }

}