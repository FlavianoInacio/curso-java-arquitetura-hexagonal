package br.com.loja.pedidos.adapter.out.persistence;

import br.com.loja.pedidos.domain.model.FormaPagamento;
import br.com.loja.pedidos.domain.model.StatusPedido;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class PedidoEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_itens", joinColumns = @JoinColumn(name = "pedido_id"))
    private List<ItemPedidoEmbeddable> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @Column(name = "pago_em")
    private Instant pagoEm;

    protected PedidoEntity() {
    }

    public PedidoEntity(UUID id, String cliente, StatusPedido status,
                        Instant criadoEm, List<ItemPedidoEmbeddable> itens,
                        FormaPagamento formaPagamento, Instant pagoEm) {
        this.id = id;
        this.cliente = cliente;
        this.status = status;
        this.criadoEm = criadoEm;
        this.itens = new ArrayList<>(itens);
        this.formaPagamento = formaPagamento;
        this.pagoEm = pagoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public List<ItemPedidoEmbeddable> getItens() {
        return itens;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public Instant getPagoEm() {
        return pagoEm;
    }
}