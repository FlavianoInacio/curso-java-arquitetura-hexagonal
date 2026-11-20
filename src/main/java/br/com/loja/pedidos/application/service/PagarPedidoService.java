package br.com.loja.pedidos.application.service;

import br.com.loja.pedidos.application.port.in.PagarPedidoUseCase;
import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.exception.PedidoNaoEncontradoException;
import br.com.loja.pedidos.domain.model.FormaPagamento;
import br.com.loja.pedidos.domain.model.Pedido;

import java.util.UUID;

public class PagarPedidoService implements PagarPedidoUseCase {

    private final PedidoRepositoryPort repository;

    public PagarPedidoService(PedidoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Pedido executar(UUID id, FormaPagamento forma) {
        var pedido = repository.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
        pedido.pagar(forma);
        return repository.salvar(pedido);
    }
}
