package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pedido")
class PedidoTest {

    private ItemPedido teclado;
    private ItemPedido mouse;

    @BeforeEach
    void setUp() {
        teclado = new ItemPedido("Teclado", 2, new BigDecimal("150.00"));
        mouse = new ItemPedido("Mouse", 1, new BigDecimal("80.00"));
    }

    @Nested
    @DisplayName("ao criar")
    class AoCriar {

        @Test
        @DisplayName("nasce com id, data de criação e status CRIADO")
        void deveCriarPedidoComStatusCriado() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));

            assertThat(pedido.getId()).isNotNull();
            assertThat(pedido.getCriadoEm()).isNotNull();
            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        }

        @Test
        @DisplayName("guarda o cliente e os itens na ordem")
        void deveGuardarClienteEItens() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado, mouse));

            assertThat(pedido.getCliente()).isEqualTo("Maria");
            assertThat(pedido.getItens())
                    .hasSize(2)
                    .extracting(ItemPedido::produto)
                    .containsExactly("Teclado", "Mouse");
        }

        @Test
        @DisplayName("calcula o total somando os subtotais")
        void deveCalcularTotal() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado, mouse));

            assertThat(pedido.total()).isEqualByComparingTo("380.00");
        }

        @Test
        @DisplayName("não aceita cliente vazio")
        void naoDeveAceitarClienteVazio() {
            assertThatThrownBy(() -> Pedido.criar(" ", List.of(teclado)))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("O cliente é obrigatório");
        }

        @Test
        @DisplayName("não aceita pedido sem itens")
        void naoDeveAceitarPedidoSemItens() {
            assertThatThrownBy(() -> Pedido.criar("Maria", List.of()))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("O pedido precisa de pelo menos um item");
        }
    }

    @Nested
    @DisplayName("ao cancelar")
    class AoCancelar {

        @Test
        @DisplayName("muda o status para CANCELADO")
        void deveCancelarPedido() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));

            pedido.cancelar();

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        }

        @Test
        @DisplayName("não pode ser cancelado duas vezes")
        void naoDeveCancelarDuasVezes() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));
            pedido.cancelar();

            assertThatThrownBy(pedido::cancelar)
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("O pedido já está cancelado");
        }
    }
}