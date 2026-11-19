package br.com.loja.pedidos.adapter.in.web.dto;

import br.com.loja.pedidos.application.port.in.BuscarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.CancelarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.CriarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.PagarPedidoUseCase;
import br.com.loja.pedidos.application.service.CancelarPedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedido;
    private final BuscarPedidoUseCase buscarPedido;
    private final CancelarPedidoUseCase cancelarPedido;
    private final PagarPedidoUseCase pagarPedido;

    public PedidoController(CriarPedidoUseCase criarPedido,
                            BuscarPedidoUseCase buscarPedido,
                            CancelarPedidoUseCase cancelarPedido,
                            PagarPedidoUseCase pagarPedido) {
        this.criarPedido = criarPedido;
        this.buscarPedido = buscarPedido;
        this.cancelarPedido = cancelarPedido;
        this.pagarPedido = pagarPedido;
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

    @PatchMapping("/{id}/cancelamento")
    public PedidoResponse cancelar(@PathVariable UUID id){
        return PedidoResponse.de(cancelarPedido.executar(id));
    }

    @PostMapping("/{id}/pagamento")
    public PedidoResponse pagar(@PathVariable UUID id, @RequestBody PagarPedidoRequest request){
        return PedidoResponse.de(pagarPedido.executar(id, request.paraFormaPagamento()));
    }
}
