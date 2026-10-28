package br.com.loja.pedidos.application.port.out;

import br.com.loja.pedidos.domain.model.Pedido;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepositoryPort {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(UUID id);
}
