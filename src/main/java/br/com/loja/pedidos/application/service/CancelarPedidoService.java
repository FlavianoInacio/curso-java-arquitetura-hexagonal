package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.in.CancelarPedidoUseCase;
import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.exception.PedidoNaoEncontradoException;
import br.com.loja.pedidos.domain.model.Pedido;

import java.util.UUID;

public class CancelarPedidoService implements CancelarPedidoUseCase {

    private final PedidoRepositoryPort repository;

    public CancelarPedidoService(PedidoRepositoryPort repository) {
        this.repository = repository;
    }


    @Override
    public Pedido executar(UUID id) {
        var pedido = repository.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
        pedido.cancelar();
        return repository.salvar(pedido);
    }
}
