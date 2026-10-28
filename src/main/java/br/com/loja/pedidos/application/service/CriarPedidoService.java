package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.in.CriarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.NovoPedidoCommand;
import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.model.Pedido;

public class CriarPedidoService implements CriarPedidoUseCase {

    private final PedidoRepositoryPort repository;


    public CriarPedidoService(PedidoRepositoryPort repository) {
        this.repository = repository;
    }


    @Override
    public Pedido executar(NovoPedidoCommand comando) {
        var pedido = comando.paraPedido();
        return repository.salvar(pedido);
    }
}
