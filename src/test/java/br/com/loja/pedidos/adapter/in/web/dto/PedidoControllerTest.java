package br.com.loja.pedidos.adapter.in.web.dto;

import br.com.loja.pedidos.application.port.in.BuscarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.CancelarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.CriarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.NovoPedidoCommand;
import br.com.loja.pedidos.domain.exception.PedidoInvalidoException;
import br.com.loja.pedidos.domain.exception.PedidoNaoEncontradoException;
import br.com.loja.pedidos.domain.model.ItemPedido;
import br.com.loja.pedidos.domain.model.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@DisplayName("PedidoController")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarPedidoUseCase criarPedido;

    @MockitoBean
    private BuscarPedidoUseCase buscarPedido;

    @MockitoBean
    private CancelarPedidoUseCase cancelarPedido;

    private Pedido pedidoDeExemplo() {
        return Pedido.criar("Maria", List.of(
                new ItemPedido("Teclado", 2, new BigDecimal("150.00")),
                new ItemPedido("Mouse", 1, new BigDecimal("80.00"))));
    }

    @Test
    @DisplayName("POST /pedidos devolve 201, o Location e o pedido criado")
    void deveCriarPedido() throws Exception {
        Pedido pedido = pedidoDeExemplo();
        when(criarPedido.executar(any(NovoPedidoCommand.class))).thenReturn(pedido);

        String corpo = """
                {
                  "cliente": "Maria",
                  "itens": [
                    { "produto": "Teclado", "quantidade": 2, "precoUnitario": 150.00 },
                    { "produto": "Mouse", "quantidade": 1, "precoUnitario": 80.00 }
                  ]
                }
                """;

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/pedidos/" + pedido.getId()))
                .andExpect(jsonPath("$.cliente").value("Maria"))
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.total").value(380.00));
    }

    @Test
    @DisplayName("POST /pedidos devolve 400 quando o pedido é inválido")
    void deveRetornar400ParaPedidoInvalido() throws Exception {
        when(criarPedido.executar(any(NovoPedidoCommand.class)))
                .thenThrow(new PedidoInvalidoException("O pedido precisa de pelo menos um item"));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cliente": "Maria", "itens": [] }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Pedido Inválido"))
                .andExpect(jsonPath("$.detail").value("O pedido precisa de pelo menos um item"));
    }

    @Test
    @DisplayName("GET /pedidos/{id} devolve o pedido")
    void deveBuscarPedido() throws Exception {
        Pedido pedido = pedidoDeExemplo();
        when(buscarPedido.executar(pedido.getId())).thenReturn(pedido);

        mockMvc.perform(get("/pedidos/{id}", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedido.getId().toString()))
                .andExpect(jsonPath("$.cliente").value("Maria"));
    }

    @Test
    @DisplayName("GET /pedidos/{id} devolve 404 quando o pedido não existe")
    void deveRetornar404QuandoNaoExistir() throws Exception {
        UUID id = UUID.randomUUID();
        when(buscarPedido.executar(id)).thenThrow(new PedidoNaoEncontradoException(id));

        mockMvc.perform(get("/pedidos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Pedido não encontrado!"));
    }

    @Test
    @DisplayName("PATCH /pedidos/{id}/cancelamento devolve o pedido cancelado")
    void deveCancelarPedido() throws Exception {
        Pedido pedido = pedidoDeExemplo();
        pedido.cancelar();
        when(cancelarPedido.executar(pedido.getId())).thenReturn(pedido);

        mockMvc.perform(patch("/pedidos/{id}/cancelamento", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }
}