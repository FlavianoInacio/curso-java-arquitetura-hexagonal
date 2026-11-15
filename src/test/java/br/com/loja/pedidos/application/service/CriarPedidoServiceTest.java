package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.in.NovoPedidoCommand;
import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import br.com.loja.pedidos.domain.model.ItemPedido;
import br.com.loja.pedidos.domain.model.Pedido;
import br.com.loja.pedidos.domain.model.StatusPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CriarPedidoService")
class CriarPedidoServiceTest {

    @Mock
    private PedidoRepositoryPort repository;

    @InjectMocks
    private CriarPedidoService service;

    @Captor
    private ArgumentCaptor<Pedido> pedidoCaptor;

    private final ItemPedido teclado = new ItemPedido("Teclado", 2, new BigDecimal("150.00"));

    @Test
    @DisplayName("cria o pedido e devolve o que o repositório salvou")
    void deveCriarESalvarOPedido() {
        when(repository.salvar(any(Pedido.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        NovoPedidoCommand comando = new NovoPedidoCommand("Maria", List.of(teclado));

        Pedido pedido = service.executar(comando);

        assertThat(pedido.getCliente()).isEqualTo("Maria");
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.total()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("chama o repositório uma única vez")
    void deveSalvarUmaUnicaVez() {
        NovoPedidoCommand comando = new NovoPedidoCommand("Maria", List.of(teclado));

        service.executar(comando);

        verify(repository, times(1)).salvar(any(Pedido.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("manda para o repositório o pedido montado a partir do comando")
    void deveSalvarPedidoComOsDadosDoComando() {
        NovoPedidoCommand comando = new NovoPedidoCommand("Maria", List.of(teclado));

        service.executar(comando);

        verify(repository).salvar(pedidoCaptor.capture());
        Pedido salvo = pedidoCaptor.getValue();
        assertThat(salvo.getCliente()).isEqualTo("Maria");
        assertThat(salvo.getItens()).containsExactly(teclado);
        assertThat(salvo.getStatus()).isEqualTo(StatusPedido.CRIADO);
    }

    @Test
    @DisplayName("não salva um pedido inválido")
    void naoDeveSalvarPedidoInvalido() {
        NovoPedidoCommand semItens = new NovoPedidoCommand("Maria", List.of());

        assertThatThrownBy(() -> service.executar(semItens))
                .isInstanceOf(PedidoInvalidoException.class);

        verify(repository, never()).salvar(any());
    }
}