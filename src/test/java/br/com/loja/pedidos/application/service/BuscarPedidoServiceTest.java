package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.exception.PedidoNaoEncontradoException;
import br.com.loja.pedidos.domain.model.ItemPedido;
import br.com.loja.pedidos.domain.model.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarPedidoService")
class BuscarPedidoServiceTest {

    @Mock
    private PedidoRepositoryPort repository;

    @InjectMocks
    private BuscarPedidoService service;

    @Test
    @DisplayName("devolve o pedido quando ele existe")
    void deveRetornarOPedidoQuandoExistir() {
        Pedido pedido = Pedido.criar("Maria", List.of(new ItemPedido("Mouse", 1, new BigDecimal("80.00"))));
        when(repository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        Pedido encontrado = service.executar(pedido.getId());

        assertThat(encontrado).isSameAs(pedido);
    }

    @Test
    @DisplayName("lança PedidoNaoEncontradoException quando o pedido não existe")
    void deveLancarExcecaoQuandoNaoExistir() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(id))
                .isInstanceOf(PedidoNaoEncontradoException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("deixa passar o erro quando o repositório falha")
    void devePropagarErroDoRepositorio() {
        when(repository.buscarPorId(any())).thenThrow(new IllegalStateException("Banco fora do ar"));

        assertThatThrownBy(() -> service.executar(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Banco fora do ar");
    }
}