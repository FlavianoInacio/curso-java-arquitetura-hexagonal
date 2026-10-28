package br.com.loja.pedidos.adapter.out.persistence;

import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.domain.model.Pedido;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PedidoJpaAdapter implements PedidoRepositoryPort {

    private final SpringDataPedidoRepository repository;

    public PedidoJpaAdapter(SpringDataPedidoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        var pedidoEntity = repository.save(PedidoPersistenceMapper.paraEntity(pedido));
        return PedidoPersistenceMapper.paraDominio(pedidoEntity);
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return repository.findById(id).map(PedidoPersistenceMapper::paraDominio);
    }
}
