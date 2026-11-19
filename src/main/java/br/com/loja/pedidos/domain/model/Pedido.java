package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Pedido {

    private final UUID id;
    private final String cliente;
    private final Instant criadoEm;
    private final List<ItemPedido> itens;
    private StatusPedido status;
    private FormaPagamento formaPagamento;
    private Instant pagoEm;

    private Pedido(UUID id, String cliente, Instant criadoEm, List<ItemPedido> itens, StatusPedido status,
                   FormaPagamento formaPagamento, Instant pagoEm) {
        this.id = id;
        this.cliente = cliente;
        this.criadoEm = criadoEm;
        this.itens = itens;
        this.status = status;
        this.formaPagamento = formaPagamento;
        this.pagoEm = pagoEm;
    }

    public static Pedido criar(String cliente, List<ItemPedido> itens) {

        if (cliente == null || cliente.isBlank()) {
            throw new PedidoInvalidoException("O cliente é obrigatório");
        }
        if (itens == null || itens.isEmpty()) {
            throw new PedidoInvalidoException("O pedido precisa de pelo menos um item");
        }
        return new Pedido(UUID.randomUUID(), cliente, Instant.now(), itens, StatusPedido.CRIADO, null, null);
    }

    public static Pedido reconstruir(UUID id,
                                     String cliente,
                                     List<ItemPedido> itens,
                                     StatusPedido status,
                                     Instant criadoEm,
                                     FormaPagamento formaPagamento,
                                     Instant pagoEm) {
        return new Pedido(id, cliente, criadoEm, itens, status, formaPagamento, pagoEm);

    }

    public BigDecimal total() {
        return itens.stream()
                .map(ItemPedido::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cancelar() {
        if (status == StatusPedido.CANCELADO) {
            throw new PedidoInvalidoException("O pedido já está cancelado");
        }
        if (status == StatusPedido.PAGO) {
            throw new PedidoInvalidoException("Não é possível cancelar um pedido pago");
        }
        this.status = StatusPedido.CANCELADO;
    }

    public void pagar(FormaPagamento forma) {
        if (forma == null) {
            throw new PedidoInvalidoException("A forma de pagamento é obrigatória");
        }
        if (status == StatusPedido.CANCELADO) {
            throw new PedidoInvalidoException("Não é possível pagar um pedido cancelado");
        }
        if (status == StatusPedido.PAGO) {
            throw new PedidoInvalidoException("O pedido já está pago");
        }
        this.status = StatusPedido.PAGO;
        this.formaPagamento = forma;
        this.pagoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public Instant getPagoEm() {
        return pagoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id) && Objects.equals(cliente, pedido.cliente) && Objects.equals(criadoEm, pedido.criadoEm) && Objects.equals(itens, pedido.itens) && status == pedido.status
                && formaPagamento == pedido.formaPagamento && Objects.equals(pagoEm, pedido.pagoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cliente, criadoEm, itens, status, formaPagamento, pagoEm);
    }
}
