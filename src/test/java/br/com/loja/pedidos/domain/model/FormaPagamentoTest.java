package br.com.loja.pedidos.domain.model;

import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FormaPagamento")
class FormaPagamentoTest {

    @Nested
    @DisplayName("valores")
    class Valores {

        @Test
        @DisplayName("aceita exatamente PIX, cartão de crédito e boleto")
        void deveTerApenasAsFormasAceitas() {
            assertThat(FormaPagamento.values())
                    .containsExactly(FormaPagamento.PIX, FormaPagamento.CARTAO_CREDITO, FormaPagamento.BOLETO);
        }
    }

    @Nested
    @DisplayName("ao converter texto")
    class AoConverterTexto {

        @ParameterizedTest(name = "\"{0}\" vira {0}")
        @EnumSource(FormaPagamento.class)
        @DisplayName("converte o nome de cada forma aceita")
        void deveConverterFormaAceita(FormaPagamento forma) {
            assertThat(FormaPagamento.de(forma.name())).isEqualTo(forma);
        }

        @ParameterizedTest(name = "[{0}]")
        @NullSource
        @ValueSource(strings = {"", " "})
        @DisplayName("exige um valor preenchido")
        void deveExigirValor(String valor) {
            assertThatThrownBy(() -> FormaPagamento.de(valor))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("A forma de pagamento é obrigatória");
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {"DINHEIRO", "pix"})
        @DisplayName("recusa forma desconhecida")
        void deveRecusarFormaDesconhecida(String valor) {
            assertThatThrownBy(() -> FormaPagamento.de(valor))
                    .isInstanceOf(PedidoInvalidoException.class)
                    .hasMessage("Forma de pagamento inválida: " + valor);
        }
    }
}
