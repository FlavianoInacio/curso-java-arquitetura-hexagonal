package br.com.loja.pedidos.adapter.in.web.dto;

import br.com.loja.pedidos.domain.model.FormaPagamento;

public record PagarPedidoRequest(String formaPagamento) {

    public FormaPagamento paraFormaPagamento() {
        return FormaPagamento.de(formaPagamento);
    }
}
