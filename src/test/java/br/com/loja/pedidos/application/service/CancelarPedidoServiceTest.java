package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelarPedidoService")
class CancelarPedidoServiceTest {

    @Mock
    private PedidoRepositoryPort repository;

    @InjectMocks
    private CancelarPedidoService service;

    private Pedido novoPedido() {
        return Pedido.criar("Maria", List.of(new ItemPedido("Teclado", 2, new BigDecimal("150.00"))));
    }

    @Test
    @DisplayName("cancela o pedido e salva")
    void deveCancelarESalvar() {
        Pedido pedido = novoPedido();
        when(repository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));
        when(repository.salvar(any(Pedido.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Pedido cancelado = service.executar(pedido.getId());

        assertThat(cancelado.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        verify(repository).salvar(pedido);
    }
}