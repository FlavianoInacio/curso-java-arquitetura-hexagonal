package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

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

        @Test
        @DisplayName("não pode cancelar pedido pago")
        void naoDeveCancelarPedidoPago() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));
            pedido.pagar(FormaPagamento.PIX);
            Instant pagoEm = pedido.getPagoEm();

            assertThatThrownBy(pedido::cancelar)
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("Não é possível cancelar um pedido pago");
            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
            assertThat(pedido.getFormaPagamento()).isEqualTo(FormaPagamento.PIX);
            assertThat(pedido.getPagoEm()).isEqualTo(pagoEm);
        }
    }

    @Nested
    @DisplayName("ao pagar")
    class AoPagar {

        @Test
        @DisplayName("nasce sem forma nem data de pagamento")
        void deveNascerSemPagamento() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));

            assertThat(pedido.getFormaPagamento()).isNull();
            assertThat(pedido.getPagoEm()).isNull();
        }

        @Test
        @DisplayName("muda o status para PAGO")
        void deveMudarStatusParaPago() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));

            pedido.pagar(FormaPagamento.PIX);

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        }

        @ParameterizedTest(name = "com {0}")
        @EnumSource(FormaPagamento.class)
        @DisplayName("guarda a forma de pagamento")
        void deveGuardarFormaPagamento(FormaPagamento forma) {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));

            pedido.pagar(forma);

            assertThat(pedido.getFormaPagamento()).isEqualTo(forma);
        }

        @Test
        @DisplayName("registra a data do pagamento")
        void deveRegistrarDataPagamento() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));
            Instant antes = Instant.now();

            pedido.pagar(FormaPagamento.BOLETO);

            assertThat(pedido.getPagoEm()).isBetween(antes, Instant.now());
        }

        @Test
        @DisplayName("não pode pagar pedido cancelado")
        void naoDevePagarPedidoCancelado() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));
            pedido.cancelar();

            assertThatThrownBy(() -> pedido.pagar(FormaPagamento.PIX))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("Não é possível pagar um pedido cancelado");
            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
            assertThat(pedido.getFormaPagamento()).isNull();
            assertThat(pedido.getPagoEm()).isNull();
        }

        @Test
        @DisplayName("não pode ser pago duas vezes")
        void naoDevePagarDuasVezes() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));
            pedido.pagar(FormaPagamento.PIX);
            Instant pagoEmOriginal = pedido.getPagoEm();

            assertThatThrownBy(() -> pedido.pagar(FormaPagamento.BOLETO))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("O pedido já está pago");
            assertThat(pedido.getFormaPagamento()).isEqualTo(FormaPagamento.PIX);
            assertThat(pedido.getPagoEm()).isEqualTo(pagoEmOriginal);
        }

        @Test
        @DisplayName("não pode pagar pedido criado há mais de 7 dias")
        void naoDevePagarPedidoExpirado() {
            Pedido pedido = Pedido.reconstruir(UUID.randomUUID(), "Maria", List.of(teclado), StatusPedido.CRIADO,
                    Instant.now().minus(8, ChronoUnit.DAYS), null, null);

            assertThatThrownBy(() -> pedido.pagar(FormaPagamento.PIX))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("O prazo para pagamento deste pedido expirou");
            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
            assertThat(pedido.getFormaPagamento()).isNull();
            assertThat(pedido.getPagoEm()).isNull();
        }

        @Test
        @DisplayName("paga pedido criado há menos de 7 dias")
        void devePagarPedidoDentroDoPrazo() {
            Pedido pedido = Pedido.reconstruir(UUID.randomUUID(), "Maria", List.of(teclado), StatusPedido.CRIADO,
                    Instant.now().minus(6, ChronoUnit.DAYS), null, null);

            pedido.pagar(FormaPagamento.PIX);

            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        }

        @Test
        @DisplayName("exige forma de pagamento")
        void deveExigirFormaPagamento() {
            Pedido pedido = Pedido.criar("Maria", List.of(teclado));

            assertThatThrownBy(() -> pedido.pagar(null))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("A forma de pagamento é obrigatória");
            assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        }
    }
}