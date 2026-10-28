package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.in.BuscarPedidoUseCase;
import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.exception.PedidoNaoEncontradoException;
import br.com.loja.pedidos.domain.model.Pedido;

import java.util.UUID;

public class BuscarPedidoService implements BuscarPedidoUseCase {

    private final PedidoRepositoryPort repository;

    public BuscarPedidoService(PedidoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Pedido executar(UUID id) {
       return repository.buscarPorId(id)
               .orElseThrow(()-> new PedidoNaoEncontradoException(id));
    }
}
