package br.com.loja.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("API de pedidos (ponta a ponta)")
class PedidoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("cria, busca e cancela um pedido de verdade")
    void deveCriarBuscarECancelarPedido() throws Exception {
        String corpo = """
                {
                  "cliente": "Maria",
                  "itens": [
                    { "produto": "Teclado", "quantidade": 2, "precoUnitario": 150.00 },
                    { "produto": "Mouse", "quantidade": 1, "precoUnitario": 80.00 }
                  ]
                }
                """;

        MvcResult criado = mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn();

        String location = criado.getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.total").value(380.00));

        mockMvc.perform(patch(location + "/cancelamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        mockMvc.perform(patch(location + "/cancelamento"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O pedido já está cancelado"));
    }

    private String criarPedido() throws Exception {
        MvcResult criado = mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": "Maria",
                                  "itens": [ { "produto": "Teclado", "quantidade": 2, "precoUnitario": 150.00 } ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return criado.getResponse().getHeader("Location");
    }

    private ResultActions pagar(String location, String forma) throws Exception {
        return mockMvc.perform(post(location + "/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"formaPagamento\": \"" + forma + "\" }"));
    }

    @Test
    @DisplayName("cria, paga e consulta um pedido")
    void deveCriarPagarEConsultarPedido() throws Exception {
        String location = criarPedido();

        pagar(location, "BOLETO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.formaPagamento").value("BOLETO"))
                .andExpect(jsonPath("$.pagoEm").isNotEmpty());
    }

    @Test
    @DisplayName("recusa pagamentos inválidos")
    void deveRecusarPagamentosInvalidos() throws Exception {
        String pago = criarPedido();
        pagar(pago, "PIX").andExpect(status().isOk());

        pagar(pago, "BOLETO")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O pedido já está pago"));
        mockMvc.perform(get(pago))
                .andExpect(jsonPath("$.formaPagamento").value("PIX"));

        String cancelado = criarPedido();
        mockMvc.perform(patch(cancelado + "/cancelamento")).andExpect(status().isOk());
        pagar(cancelado, "PIX")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Não é possível pagar um pedido cancelado"));

        pagar("/pedidos/" + UUID.randomUUID(), "PIX")
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("não cancela pedido pago")
    void naoDeveCancelarPedidoPago() throws Exception {
        String location = criarPedido();
        pagar(location, "CARTAO_CREDITO").andExpect(status().isOk());

        mockMvc.perform(patch(location + "/cancelamento"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Não é possível cancelar um pedido pago"));

        mockMvc.perform(get(location))
                .andExpect(jsonPath("$.status").value("PAGO"));
    }
}