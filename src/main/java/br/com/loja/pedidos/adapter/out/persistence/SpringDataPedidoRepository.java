package br.com.loja.pedidos.adapter.out.persistence;

import br.com.loja.pedidos.application.port.out.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataPedidoRepository extends JpaRepository<PedidoEntity, UUID> {
}
