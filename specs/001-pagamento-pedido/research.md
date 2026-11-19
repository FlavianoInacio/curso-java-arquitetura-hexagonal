# Research: Pagamento de Pedido

Não há itens `NEEDS CLARIFICATION` no Technical Context; este documento registra as decisões de
design tomadas a partir do código atual, da constituição e da spec.

## D1. Onde fica a regra de pagamento

- **Decision**: método `Pedido.pagar(FormaPagamento forma)` em `domain.model`, que valida o status,
  define `status = PAGO`, `formaPagamento = forma` e `pagoEm = Instant.now()`.
- **Rationale**: Princípio II (regras no domínio) e pedido explícito do usuário. Segue o mesmo
  padrão de `Pedido.criar()` (que usa `Instant.now()`) e `Pedido.cancelar()`.
- **Alternatives considered**: receber o instante como parâmetro (`pagar(forma, agora)`) para
  testes determinísticos. Rejeitado por divergir do padrão atual; os testes verificam que `pagoEm`
  fica entre um instante anterior e um posterior à chamada.

## D2. Representação da forma de pagamento

- **Decision**: enum `FormaPagamento { PIX, CARTAO_CREDITO, BOLETO }` em `domain.model`, com
  fábrica `FormaPagamento.de(String valor)` que lança `PedidoInvalidoException` para valor nulo,
  vazio ou desconhecido.
- **Rationale**: conjunto fechado (FR-002). A conversão via fábrica do domínio garante que forma
  inválida vire 400 com `ProblemDetail` e mensagem em português (Princípio IV, FR-008, FR-011), em
  vez de um erro genérico de desserialização do Jackson.
- **Alternatives considered**: usar o enum direto no DTO de entrada. Rejeitado: valor inválido
  geraria `HttpMessageNotReadableException` com mensagem técnica em inglês.

## D3. Corpo ausente ou malformado

- **Decision**: adicionar ao `GlobalExceptionHandler` um handler de
  `HttpMessageNotReadableException` → 400 `ProblemDetail` com título "Requisição inválida" e
  detalhe "O corpo da requisição é inválido".
- **Rationale**: o endpoint passa a exigir corpo; sem o handler, um POST sem corpo devolveria a
  página de erro padrão do Spring, fora do padrão `ProblemDetail`.
- **Alternatives considered**: habilitar `spring.mvc.problemdetails.enabled`. Rejeitado: muda o
  formato de outros erros do framework e a mensagem continuaria em inglês.

## D4. Persistência sem tabela nova

- **Decision**: duas colunas anuláveis na tabela `pedidos`, mapeadas em `PedidoEntity`:
  `forma_pagamento` (`@Enumerated(STRING)`) e `pago_em` (`Instant`). `PedidoPersistenceMapper`
  passa os campos nos dois sentidos; `Pedido.reconstruir(...)` ganha os dois parâmetros.
- **Rationale**: pedido explícito do usuário (sem tabela nova); `ddl-auto: create-drop` no H2 cria
  as colunas automaticamente, sem migração.
- **Alternatives considered**: entidade/tabela `pagamentos` separada. Rejeitada pelo usuário e
  desnecessária, pois há no máximo um pagamento por pedido.

## D5. Contrato HTTP

- **Decision**: `POST /pedidos/{id}/pagamento` com corpo `{"formaPagamento": "PIX"}`, resposta
  200 com o `PedidoResponse` atualizado. `PedidoResponse` ganha `formaPagamento` e `pagoEm`
  (nulos enquanto não pago).
- **Rationale**: ação como sub-recurso (Princípio IV); verbo POST definido pelo usuário. Devolver
  o pedido segue o padrão de `PATCH /pedidos/{id}/cancelamento`.
- **Alternatives considered**: 201 Created com recurso de pagamento próprio. Rejeitado: não há
  recurso de pagamento separado (D4).
- **Observação**: o cancelamento existente usa PATCH e o pagamento usará POST. A diferença de verbo
  é mantida porque o usuário definiu o POST e o PATCH já é coberto por testes existentes.

## D6. Pagamentos simultâneos (edge case da spec, SC-002)

- **Decision**: não implementar controle de concorrência nesta entrega; registrar como limitação
  conhecida.
- **Rationale**: aplicação de instância única com H2 em memória e escopo didático (Princípio V).
  A regra sequencial ("já está pago") é garantida pelo domínio. Travamento otimista exigiria
  `@Version` na entidade, carregar a versão no domínio e um novo mapeamento de erro (409), o que
  amplia o escopo além do pedido.
- **Alternatives considered**: `@Version` em `PedidoEntity` com versão propagada pelo domínio.
  Fica como evolução futura caso a concorrência real se torne requisito.

## D7. Cancelamento de pedido pago

- **Decision**: `Pedido.cancelar()` passa a recusar status PAGO com
  `PedidoInvalidoException("Não é possível cancelar um pedido pago")`.
- **Rationale**: clarificação da spec (FR-012, US3).
