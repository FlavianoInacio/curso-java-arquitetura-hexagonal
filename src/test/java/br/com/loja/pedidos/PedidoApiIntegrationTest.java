package br.com.loja.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
}