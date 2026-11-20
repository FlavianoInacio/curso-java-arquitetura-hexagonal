package br.com.loja.pedidos.config;

import br.com.loja.pedidos.application.port.in.BuscarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.CancelarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.CriarPedidoUseCase;
import br.com.loja.pedidos.application.port.in.PagarPedidoUseCase;
import br.com.loja.pedidos.application.port.out.PedidoRepositoryPort;
import br.com.loja.pedidos.application.service.BuscarPedidoService;
import br.com.loja.pedidos.application.service.CancelarPedidoService;
import br.com.loja.pedidos.application.service.CriarPedidoService;
import br.com.loja.pedidos.application.service.PagarPedidoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CriarPedidoUseCase criarPedidoUseCase(PedidoRepositoryPort repository){
        return new CriarPedidoService(repository);
    }

    @Bean
    public BuscarPedidoUseCase buscarPedidoUseCase(PedidoRepositoryPort repository){
        return new BuscarPedidoService(repository);
    }

    @Bean
    public CancelarPedidoUseCase cancelarPedidoUseCase(PedidoRepositoryPort repository){
        return new CancelarPedidoService(repository);
    }

    @Bean
    public PagarPedidoUseCase pagarPedidoUseCase(PedidoRepositoryPort repository){
        return new PagarPedidoService(repository);
    }

}
