package br.com.loja.pedidos.application.port.in;

import br.com.loja.pedidos.domain.model.Pedido;

public interface CriarPedidoUseCase {

    Pedido executar(NovoPedidoCommand comando);
}
