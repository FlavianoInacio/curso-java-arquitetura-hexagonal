package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import br.com.loja.pedidos.domain.exception.PedidoNaoEncontradoException;
import br.com.loja.pedidos.domain.model.FormaPagamento;
import br.com.loja.pedidos.domain.model.ItemPedido;
import br.com.loja.pedidos.domain.model.Pedido;
import br.com.loja.pedidos.domain.model.StatusPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagarPedidoService")
class PagarPedidoServiceTest {

    @Mock
    private PedidoRepositoryPort repository;

    @InjectMocks
    private PagarPedidoService service;

    private Pedido novoPedido() {
        return Pedido.criar("Maria", List.of(new ItemPedido("Teclado", 2, new BigDecimal("150.00"))));
    }

    @Test
    @DisplayName("paga o pedido, salva uma vez e devolve o que foi salvo")
    void devePagarESalvar() {
        Pedido pedido = novoPedido();
        when(repository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));
        when(repository.salvar(any(Pedido.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Pedido pago = service.executar(pedido.getId(), FormaPagamento.PIX);

        assertThat(pago.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pago.getFormaPagamento()).isEqualTo(FormaPagamento.PIX);
        assertThat(pago.getPagoEm()).isNotNull();
        verify(repository, times(1)).salvar(pedido);
    }

    @Test
    @DisplayName("lança PedidoNaoEncontradoException quando o pedido não existe e não salva")
    void deveFalharQuandoPedidoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(id, FormaPagamento.PIX))
                .isInstanceOf(PedidoNaoEncontradoException.class);
        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("não salva pedido com prazo de pagamento expirado")
    void naoDeveSalvarPedidoExpirado() {
        Pedido pedido = Pedido.reconstruir(UUID.randomUUID(), "Maria",
                List.of(new ItemPedido("Teclado", 2, new BigDecimal("150.00"))), StatusPedido.CRIADO,
                Instant.now().minus(8, ChronoUnit.DAYS), null, null);
        when(repository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.executar(pedido.getId(), FormaPagamento.PIX))
                .isInstanceOf(PedidoInvalidoException.class)
                .hasMessage("O prazo para pagamento deste pedido expirou");
        verify(repository, never()).salvar(any());
    }

    @Test
    @DisplayName("não salva quando o domínio recusa o pagamento")
    void naoDeveSalvarQuandoRegraVioladaNoDominio() {
        Pedido pedido = novoPedido();
        pedido.cancelar();
        when(repository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.executar(pedido.getId(), FormaPagamento.PIX))
                .isInstanceOf(PedidoInvalidoException.class);
        verify(repository, never()).salvar(any());
    }
}
