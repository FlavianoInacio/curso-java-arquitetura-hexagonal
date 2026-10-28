package br.com.loja.pedidos.application.port.in;

import br.com.loja.pedidos.domain.model.Pedido;

import java.util.UUID;

public interface BuscarPedidoUseCase {
    
    Pedido executar(UUID id);
}
