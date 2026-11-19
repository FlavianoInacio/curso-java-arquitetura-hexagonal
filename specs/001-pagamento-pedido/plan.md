# Implementation Plan: Pagamento de Pedido

**Branch**: `feature/pagamento` | **Date**: 2026-11-19 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-pagamento-pedido/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Permitir que o cliente pague um pedido CRIADO por PIX, cartão de crédito ou boleto. A regra fica em
`Pedido.pagar(FormaPagamento)`, que muda o status para PAGO e registra forma e data do pagamento;
pedidos CANCELADOS ou já PAGOS são recusados, e `Pedido.cancelar()` passa a recusar pedidos PAGOS.
Um novo caso de uso `PagarPedidoUseCase` (orquestração) é exposto em
`POST /pedidos/{id}/pagamento` com a forma de pagamento no corpo. Forma e data são persistidas como
duas colunas novas na tabela `pedidos`, sem tabela nova. A estrutura de pacotes atual é mantida,
exceto pelas classes JPA (`PedidoEntity`, `ItemPedidoEmbeddable`), que saem de
`application.port.out` para `adapter.out.persistence` para cumprir o Princípio I.

## Technical Context

**Language/Version**: Java 20

**Primary Dependencies**: Spring Boot 3.5.6 (web, data-jpa); nenhuma dependência nova

**Storage**: H2 em memória, `ddl-auto: create-drop` (sem migração)

**Testing**: JUnit 5, AssertJ, Mockito, `@WebMvcTest` + `@MockitoBean`, `@DataJpaTest`,
`@SpringBootTest`; cobertura com JaCoCo

**Target Platform**: serviço web JVM (execução local)

**Project Type**: web-service (API REST, projeto Maven único)

**Performance Goals**: não aplicável (escopo didático); operação síncrona simples

**Constraints**: manter os pacotes atuais (exceto a mudança das classes JPA para o adapter); não quebrar testes existentes; sem integração externa
real; código e mensagens em português

**Scale/Scope**: 1 caso de uso novo, 1 endpoint novo, 1 enum novo, 2 colunas novas

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Verificação | Status |
|-----------|-------------|--------|
| I. Arquitetura Hexagonal | `FormaPagamento` e `pagar()` em `domain.model`, Java puro. `PagarPedidoUseCase` em `application.port.in`, `PagarPedidoService` em `application.service`, registrado como `@Bean` em `config.UseCaseConfig`, sem `@Service`. Acesso a dados só via `PedidoRepositoryPort` (sem alteração da porta). Classes JPA movidas para `adapter.out.persistence`. | ✅ |
| II. Regras no Domínio | Validações de status e forma em `Pedido`/`FormaPagamento`, com `PedidoInvalidoException`. Inexistente → `PedidoNaoEncontradoException` no service. Service só busca, delega e salva. | ✅ |
| III. Testes Primeiro | Cada fase começa pelos testes: domínio (`PedidoTest` com novos `@Nested` "ao pagar", `FormaPagamentoTest`), `PagarPedidoServiceTest` (Mockito), `PedidoControllerTest` (`@WebMvcTest`), `PedidoJpaAdapterTest` (`@DataJpaTest`), `PedidoApiIntegrationTest` (`@SpringBootTest`). Testes existentes preservados; `mvn test` verde ao fim de cada fase. | ✅ |
| IV. API REST Consistente | Sub-recurso `/pedidos/{id}/pagamento`; erros via `GlobalExceptionHandler` como `ProblemDetail` (400 regra violada, 404 inexistente, 400 corpo inválido). | ✅ |
| V. Simplicidade | Nenhuma dependência nova; sem gateway de pagamento real; sem tabela nova; concorrência não tratada (research D6). Nomes e mensagens em português. | ✅ |

**Resultado (pré-Phase 0)**: aprovado. **Re-check pós-Phase 1**: aprovado. O design em
[data-model.md](data-model.md) e [contracts/pagamento-api.md](contracts/pagamento-api.md) não
introduz violações novas.

## Project Structure

### Documentation (this feature)

```text
specs/001-pagamento-pedido/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── pagamento-api.md
├── checklists/
│   └── requirements.md
└── tasks.md             # gerado por /speckit-tasks
```

### Source Code (repository root)

```text
src/main/java/br/com/loja/pedidos/
├── domain/model/
│   ├── FormaPagamento.java              # NOVO
│   ├── StatusPedido.java                # + PAGO
│   └── Pedido.java                      # + pagar(), cancelar() recusa PAGO, novos campos
├── application/
│   ├── port/in/PagarPedidoUseCase.java  # NOVO
│   └── service/PagarPedidoService.java  # NOVO
├── adapter/
│   ├── in/web/GlobalExceptionHandler.java          # + HttpMessageNotReadableException
│   ├── in/web/dto/PagarPedidoRequest.java          # NOVO
│   ├── in/web/dto/PedidoController.java            # + POST /{id}/pagamento
│   ├── in/web/dto/PedidoResponse.java              # + formaPagamento, pagoEm
│   ├── out/persistence/PedidoEntity.java           # MOVIDA de application/port/out + forma_pagamento, pago_em
│   ├── out/persistence/ItemPedidoEmbeddable.java   # MOVIDA de application/port/out
│   └── out/persistence/PedidoPersistenceMapper.java # mapeia novos campos
└── config/UseCaseConfig.java            # + @Bean pagarPedidoUseCase

src/test/java/br/com/loja/pedidos/
├── domain/model/PedidoTest.java                     # + @Nested "ao pagar", cancelar pago
├── domain/model/FormaPagamentoTest.java             # NOVO
├── application/service/PagarPedidoServiceTest.java  # NOVO
├── adapter/in/web/dto/PedidoControllerTest.java     # + cenários de pagamento
├── adapter/out/persistence/PedidoJpaAdapterTest.java # + persiste forma e data
└── PedidoApiIntegrationTest.java                    # + fluxo criar → pagar → recusas
```

**Structure Decision**: projeto Maven único, mantendo os pacotes atuais conforme pedido. A única
exceção é mover `PedidoEntity` e `ItemPedidoEmbeddable` para `adapter.out.persistence`, exigido
pelo Princípio I (não negociável). Nenhum teste referencia essas classes. O `PedidoController`
permanece em `adapter.in.web.dto`, que está dentro de `adapter` e é compatível com o princípio.

### Ordem de implementação (fases, cada uma com testes primeiro e `mvn test` verde ao final)

0. **Refatoração estrutural**: mover as classes JPA para `adapter.out.persistence`, sem mudar
   comportamento; os testes existentes servem de rede de segurança.
1. **Domínio**: `FormaPagamento`, `StatusPedido.PAGO`, `Pedido.pagar()`, recusa em `cancelar()`,
   novos campos e `reconstruir(...)` estendido.
2. **Caso de uso**: `PagarPedidoUseCase` (`Pedido executar(UUID id, FormaPagamento forma)`),
   `PagarPedidoService`, `@Bean` em `UseCaseConfig`.
3. **Persistência**: colunas em `PedidoEntity`, mapper nos dois sentidos.
4. **Web**: `PagarPedidoRequest(String formaPagamento)`, que converte com `FormaPagamento.de(...)`,
   endpoint no controller, campos novos em `PedidoResponse` e handler de corpo inválido.
5. **Ponta a ponta**: cenários no `PedidoApiIntegrationTest` e validação pelo
   [quickstart.md](quickstart.md).

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Pagamentos simultâneos não são protegidos (edge case da spec / SC-002) | Escopo didático, instância única, H2 em memória | `@Version` exigiria propagar a versão pelo domínio e criar um mapeamento 409 novo, ampliando o escopo (research D6) |
