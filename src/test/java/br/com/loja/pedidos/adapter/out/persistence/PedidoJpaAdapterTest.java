package br.com.loja.pedidos.adapter.out.persistence;

import br.com.loja.pedidos.domain.model.FormaPagamento;
import br.com.loja.pedidos.domain.model.ItemPedido;
import br.com.loja.pedidos.domain.model.Pedido;
import br.com.loja.pedidos.domain.model.StatusPedido;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(PedidoJpaAdapter.class)
@DisplayName("PedidoJpaAdapter")
class PedidoJpaAdapterTest {

    @Autowired
    private PedidoJpaAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    private Pedido novoPedido() {
        return Pedido.criar("Maria", List.of(
                new ItemPedido("Teclado", 2, new BigDecimal("150.00")),
                new ItemPedido("Mouse", 1, new BigDecimal("80.00"))));
    }

    private void limparCache(){
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void deveSalvarEbuscarPedido(){
        var pedidoOriginal = novoPedido();

        adapter.salvar(pedidoOriginal);
        limparCache();
        var pedidoEncontrato = adapter.buscarPorId(pedidoOriginal.getId()).orElseThrow();

        assertThat(pedidoEncontrato.getId()).isEqualTo(pedidoOriginal.getId());
        assertThat(pedidoEncontrato.getCliente()).isEqualTo(pedidoOriginal.getCliente());
        assertThat(pedidoEncontrato.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedidoEncontrato.getItens()).hasSize(2);
        assertThat(pedidoEncontrato.total()).isEqualByComparingTo("380");
        assertThat(pedidoEncontrato.getFormaPagamento()).isNull();
        assertThat(pedidoEncontrato.getPagoEm()).isNull();

    }

    @Test
    @DisplayName("salva e recupera a forma e a data do pagamento")
    void deveSalvarEBuscarPedidoPago() {
        var pedidoOriginal = novoPedido();
        pedidoOriginal.pagar(FormaPagamento.CARTAO_CREDITO);

        adapter.salvar(pedidoOriginal);
        limparCache();
        var pedidoEncontrado = adapter.buscarPorId(pedidoOriginal.getId()).orElseThrow();

        assertThat(pedidoEncontrado.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedidoEncontrado.getFormaPagamento()).isEqualTo(FormaPagamento.CARTAO_CREDITO);
        // o banco guarda microssegundos; Instant.now() pode ter mais precisão
        assertThat(pedidoEncontrado.getPagoEm()).isCloseTo(pedidoOriginal.getPagoEm(), within(1, ChronoUnit.MICROS));
    }


}