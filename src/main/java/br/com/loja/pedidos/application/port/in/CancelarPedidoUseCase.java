package br.com.loja.pedidos.application.port.in;

import br.com.loja.pedidos.domain.model.Pedido;

import java.util.UUID;

public interface CancelarPedidoUseCase {

    Pedido executar(UUID id);

}
