package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;

import java.util.Arrays;

public enum FormaPagamento {
    PIX,
    CARTAO_CREDITO,
    BOLETO;

    public static FormaPagamento de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new PedidoInvalidoException("A forma de pagamento é obrigatória");
        }
        return Arrays.stream(values())
                .filter(forma -> forma.name().equals(valor))
                .findFirst()
                .orElseThrow(() -> new PedidoInvalidoException("Forma de pagamento inválida: " + valor));
    }
}
