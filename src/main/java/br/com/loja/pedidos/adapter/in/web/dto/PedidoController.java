package br.com.loja.pedidos.adapter.in.web.dto;

import br.com.loja.pedidos.application.port.in.BuscarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.CriarPedidoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedido;
    private final BuscarPedidoUseCase buscarPedido;


    public PedidoController(CriarPedidoUseCase criarPedido, BuscarPedidoUseCase buscarPedido) {
        this.criarPedido = criarPedido;
        this.buscarPedido = buscarPedido;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@RequestBody CriarPedidoRequest request){
        var pedido = criarPedido.executar(request.paraCommand());
        var localtion = URI.create("/pedidos/" + pedido.getId());
        return ResponseEntity.created(localtion).body(PedidoResponse.de(pedido));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable UUID id){
        return PedidoResponse.de(buscarPedido.executar(id));
    }
}
