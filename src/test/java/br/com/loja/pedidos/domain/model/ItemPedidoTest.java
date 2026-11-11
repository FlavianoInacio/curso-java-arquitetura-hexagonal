package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ItemPedido")
class ItemPedidoTest {

    @Test
    @DisplayName("calcula o subtotal multiplicando o preço pela quantidade")
    void deveCalcularSubtotal() {
        // Arrange
        ItemPedido item = new ItemPedido("Teclado", 2, new BigDecimal("150.00"));

        // Act
        BigDecimal subtotal = item.subTotal();

        // Assert
        assertEquals(new BigDecimal("300.00"), subtotal);
    }

    @ParameterizedTest(name = "{0} x {1} = {2}")
    @CsvSource({
            "1, 10.00, 10.00",
            "2, 150.00, 300.00",
            "3, 33.33, 99.99"
    })
    @DisplayName("calcula o subtotal para várias quantidades e preços")
    void deveCalcularSubtotalParaVariosCasos(int quantidade, BigDecimal preco, BigDecimal esperado) {
        ItemPedido item = new ItemPedido("Produto", quantidade, preco);

        assertThat(item.subTotal()).isEqualByComparingTo(esperado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("não aceita produto vazio")
    void naoDeveAceitarProdutoVazio(String produto) {
        assertThatThrownBy(() -> new ItemPedido(produto, 1, BigDecimal.TEN))
                .isInstanceOf(PedidoInvalidoException.class)
                .hasMessageContaining("produto");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("não aceita quantidade menor ou igual a zero")
    void naoDeveAceitarQuantidadeInvalida(int quantidade) {
        assertThatThrownBy(() -> new ItemPedido("Teclado", quantidade, BigDecimal.TEN))
                .isInstanceOf(PedidoInvalidoException.class)
                .hasMessage("A quantidade deve ser maior que zero");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-50"})
    @DisplayName("não aceita preço menor ou igual a zero")
    void naoDeveAceitarPrecoInvalido(String preco) {
        assertThatThrownBy(() -> new ItemPedido("Teclado", 1, new BigDecimal(preco)))
                .isInstanceOf(PedidoInvalidoException.class)
                .hasMessage("O preço unitário deve ser maior que zero");
    }
}