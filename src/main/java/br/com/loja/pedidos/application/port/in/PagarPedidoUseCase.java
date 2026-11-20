package br.com.loja.pedidos.application.port.in;

import br.com.loja.pedidos.domain.model.FormaPagamento;
import br.com.loja.pedidos.domain.model.Pedido;

import java.util.UUID;

public interface PagarPedidoUseCase {

    Pedido executar(UUID id, FormaPagamento forma);
}
