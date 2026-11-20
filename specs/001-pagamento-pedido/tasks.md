---

description: "Lista de tarefas para implementar o Pagamento de Pedido"
---

# Tasks: Pagamento de Pedido

**Input**: Design documents from `/specs/001-pagamento-pedido/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/pagamento-api.md, quickstart.md

**Tests**: OBRIGATÓRIOS (Constituição, Princípio III, Testes Primeiro). Em cada fase os testes são
escritos antes e DEVEM falhar antes da implementação. `mvn test` verde é obrigatório em cada
checkpoint, sem desativar testes existentes.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)

## Path Conventions

Projeto Maven único. Abreviações usadas nas descrições:

- `main/` = `src/main/java/br/com/loja/pedidos/`
- `test/` = `src/test/java/br/com/loja/pedidos/`

Pacotes atuais mantidos (plan.md › Structure Decision): o controller e os DTOs continuam em
`adapter/in/web/dto/`. `PedidoEntity` e `ItemPedidoEmbeddable` são movidas para
`adapter/out/persistence/` (T002).

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: confirmar a linha de base antes de qualquer mudança

- [X] T001 Rodar `mvn test` na raiz do repositório e confirmar que todos os testes existentes passam (linha de base). Registrar a contagem de testes para comparar nos checkpoints (`pom.xml`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: alinhar a estrutura à constituição e criar os tipos de domínio compartilhados por todas
as histórias

**⚠️ CRITICAL**: nenhuma história começa antes desta fase

- [X] T002 Mover `main/application/port/out/PedidoEntity.java` e `main/application/port/out/ItemPedidoEmbeddable.java` para `main/adapter/out/persistence/`, ajustando `package` e imports em `PedidoPersistenceMapper`, `PedidoJpaAdapter` e `SpringDataPedidoRepository` (todos em `main/adapter/out/persistence/`). Sem mudar comportamento. Rodar `mvn test`: verde, com a mesma contagem da T001
- [X] T003 [P] Escrever `test/domain/model/FormaPagamentoTest.java` (JUnit 5 + AssertJ, `@DisplayName` em português), organizado com `@Nested` (`@DisplayName("valores")` e `@DisplayName("ao converter texto")`): os valores do enum são exatamente PIX, CARTAO_CREDITO e BOLETO, e `FormaPagamento.de("PIX")`, `de("CARTAO_CREDITO")` e `de("BOLETO")` devolvem o valor correspondente. Confirmar que não compila ou falha
- [X] T004 Criar o enum `main/domain/model/FormaPagamento.java` com os valores de data-model.md › `FormaPagamento` e o método estático `de(String valor)` para o caminho feliz (comparação exata com o nome do enum). Java puro, sem anotações de framework. Faz T003 passar
- [X] T005 [P] Adicionar `PAGO` a `main/domain/model/StatusPedido.java` (data-model.md › `StatusPedido`), mantendo `CRIADO` e `CANCELADO`
- [X] T006 Adicionar a `main/domain/model/Pedido.java` os campos `formaPagamento` (data-model.md › `Pedido.formaPagamento`) e `pagoEm` (data-model.md › `Pedido.pagoEm`), nulos na criação, com getters `getFormaPagamento()` e `getPagoEm()`. Estender `reconstruir(...)` com os dois parâmetros finais (data-model.md › `Pedido.reconstruir`) e incluir os campos em `equals`/`hashCode`
- [X] T007 Atualizar `main/adapter/out/persistence/PedidoPersistenceMapper.java` para a nova assinatura de `Pedido.reconstruir(...)`, passando `null` nos dois parâmetros novos por enquanto (o mapeamento real entra na T015). Necessário para compilar
- [X] T008 Rodar `mvn test`: verde, com todos os testes existentes passando

**Checkpoint**: estrutura conforme a constituição e domínio pronto para receber o pagamento

---

## Phase 3: User Story 1 - Pagar um pedido em aberto (Priority: P1) 🎯 MVP

**Goal**: um pedido CRIADO pode ser pago por PIX, cartão de crédito ou boleto; passa a PAGO, guarda
a forma e a data, e isso aparece na consulta.

**Independent Test**: criar um pedido, chamar `POST /pedidos/{id}/pagamento` com cada forma e
consultar `GET /pedidos/{id}`: status PAGO, `formaPagamento` e `pagoEm` preenchidos.

### Tests for User Story 1 ⚠️

> **NOTE: escrever estes testes PRIMEIRO e garantir que FALHAM antes da implementação**

- [X] T009 [P] [US1] Em `test/domain/model/PedidoTest.java`, adicionar `@Nested @DisplayName("ao pagar")` com: "muda o status para PAGO", "guarda a forma de pagamento" (parametrizado para as três formas) e "registra a data do pagamento" (`pagoEm` entre um instante capturado antes e outro depois da chamada). Um pedido recém-criado tem `formaPagamento` e `pagoEm` nulos. Não alterar os testes existentes
- [X] T010 [P] [US1] Criar `test/application/service/PagarPedidoServiceTest.java` (Mockito, mock de `PedidoRepositoryPort`): busca o pedido por id, chama `pagar`, salva uma única vez e devolve o que o repositório salvou com status PAGO e a forma informada
- [X] T011 [P] [US1] Em `test/adapter/out/persistence/PedidoJpaAdapterTest.java` (`@DataJpaTest`), adicionar teste que salva um pedido pago e, ao buscar, recupera status PAGO, `formaPagamento` e `pagoEm` iguais aos salvos. Adicionar também a verificação de que um pedido não pago volta com os dois campos nulos
- [X] T012 [P] [US1] Em `test/adapter/in/web/dto/PedidoControllerTest.java` (`@WebMvcTest` + `@MockitoBean PagarPedidoUseCase`), adicionar "POST /pedidos/{id}/pagamento devolve 200 e o pedido pago": corpo `{"formaPagamento":"PIX"}`, `$.status` = PAGO, `$.formaPagamento` = PIX e `$.pagoEm` presente (contracts/pagamento-api.md)

### Implementation for User Story 1

- [X] T013 [US1] Implementar `Pedido.pagar(FormaPagamento forma)` em `main/domain/model/Pedido.java` (caminho feliz de data-model.md › `Pedido.pagar`): `status = PAGO`, `formaPagamento = forma` e `pagoEm = Instant.now()`. Faz T009 passar
- [X] T014 [US1] Criar a interface `main/application/port/in/PagarPedidoUseCase.java` com `Pedido executar(UUID id, FormaPagamento forma)` e a classe `main/application/service/PagarPedidoService.java`, sem `@Service`: busca via `PedidoRepositoryPort.buscarPorId`, lança `PedidoNaoEncontradoException(id)` se ausente, chama `pedido.pagar(forma)` e devolve `repository.salvar(pedido)`, no mesmo padrão de `CancelarPedidoService`. Faz T010 passar
- [X] T015 [US1] Em `main/adapter/out/persistence/PedidoEntity.java`, adicionar a coluna `forma_pagamento` (`@Enumerated(STRING)`, anulável; data-model.md › `PedidoEntity.forma_pagamento`) e a coluna `pago_em` (`Instant`, anulável; data-model.md › `PedidoEntity.pago_em`), com getters e o construtor estendido. Em `main/adapter/out/persistence/PedidoPersistenceMapper.java`, mapear os dois campos nos dois sentidos, substituindo os `null` da T007. Faz T011 passar
- [X] T016 [US1] Registrar `@Bean PagarPedidoUseCase pagarPedidoUseCase(PedidoRepositoryPort)` em `main/config/UseCaseConfig.java`
- [X] T017 [US1] Criar o record `main/adapter/in/web/dto/PagarPedidoRequest.java` com `String formaPagamento` e o método `FormaPagamento paraFormaPagamento()`, que delega a `FormaPagamento.de(...)` (research.md D2)
- [X] T018 [US1] Em `main/adapter/in/web/dto/PedidoResponse.java`, adicionar ao final os campos `String formaPagamento` (nome do enum ou `null`) e `Instant pagoEm`, preenchidos em `de(Pedido)`. Os campos existentes não mudam
- [X] T019 [US1] Em `main/adapter/in/web/dto/PedidoController.java`, injetar `PagarPedidoUseCase` e adicionar `@PostMapping("/{id}/pagamento")`, que recebe `@RequestBody PagarPedidoRequest` e devolve 200 com `PedidoResponse.de(...)`. Ajustar a montagem do controller em `PedidoControllerTest` se necessário, sem mudar as asserções existentes. Faz T012 passar
- [X] T020 [US1] Em `test/PedidoApiIntegrationTest.java` (`@SpringBootTest`), adicionar o teste "cria, paga e consulta um pedido": criar, chamar `POST {location}/pagamento` com BOLETO, e `GET {location}` deve devolver status PAGO, `formaPagamento` BOLETO e `pagoEm` preenchido
- [X] T021 [US1] Rodar `mvn test`: verde, com todos os testes antigos e novos passando

**Checkpoint**: MVP. O pagamento funciona de ponta a ponta

---

## Phase 4: User Story 2 - Impedir pagamentos inválidos (Priority: P2)

**Goal**: recusar o pagamento de pedido cancelado, já pago, inexistente, sem forma ou com forma não
aceita, com `ProblemDetail` e mensagens em português, sem alterar o pedido.

**Independent Test**: as tentativas inválidas devolvem 400 ou 404 com os `detail` de
contracts/pagamento-api.md, e uma consulta posterior mostra o pedido inalterado.

### Tests for User Story 2 ⚠️

- [X] T022 [P] [US2] Em `test/domain/model/PedidoTest.java`, dentro de "ao pagar", adicionar: "não pode pagar pedido cancelado" ("Não é possível pagar um pedido cancelado", status continua CANCELADO); "não pode ser pago duas vezes" ("O pedido já está pago", com forma e data originais inalteradas); e "exige forma de pagamento" (`pagar(null)` → "A forma de pagamento é obrigatória", status continua CRIADO). Todos esperam `PedidoInvalidoException`
- [X] T023 [P] [US2] Em `test/domain/model/FormaPagamentoTest.java`, no `@Nested` "ao converter texto", adicionar: `de(null)`, `de("")` e `de(" ")` → `PedidoInvalidoException` "A forma de pagamento é obrigatória"; `de("DINHEIRO")` e `de("pix")` → "Forma de pagamento inválida: <valor>"
- [X] T024 [P] [US2] Em `test/application/service/PagarPedidoServiceTest.java`, adicionar: pedido inexistente → `PedidoNaoEncontradoException` e `salvar` nunca chamado; e regra violada no domínio (pedido cancelado) → `PedidoInvalidoException` propagada e `salvar` nunca chamado
- [X] T025 [P] [US2] Em `test/adapter/in/web/dto/PedidoControllerTest.java`, adicionar: use case lançando `PedidoInvalidoException` → 400 com `$.title` "Pedido Inválido"; `PedidoNaoEncontradoException` → 404; corpo `{"formaPagamento":"DINHEIRO"}` → 400 com `$.detail` "Forma de pagamento inválida: DINHEIRO"; corpo `{}` → 400 com "A forma de pagamento é obrigatória"; sem corpo → 400 com `$.title` "Requisição inválida" (contracts/pagamento-api.md)

### Implementation for User Story 2

- [X] T026 [US2] Em `main/domain/model/Pedido.java`, completar `pagar(...)` com as validações de data-model.md › `Pedido.pagar`, na ordem: forma nula, CANCELADO, PAGO. Todas antes de qualquer alteração de estado (FR-010). Faz T022 passar
- [X] T027 [US2] Em `main/domain/model/FormaPagamento.java`, completar `de(String)` conforme data-model.md › `FormaPagamento.de` (nulo ou em branco → obrigatória; desconhecido → inválida, ambos com `PedidoInvalidoException`). Faz T023 passar
- [X] T028 [US2] Em `main/adapter/in/web/GlobalExceptionHandler.java`, adicionar `@ExceptionHandler(HttpMessageNotReadableException.class)` → `ProblemDetail` 400 com título "Requisição inválida" e detalhe "O corpo da requisição é inválido" (research.md D3). Faz T025 passar
- [X] T029 [US2] Em `test/PedidoApiIntegrationTest.java`, adicionar o teste "recusa pagamentos inválidos": pagar duas vezes (2º → 400 "O pedido já está pago", e o GET mantém a forma original); pagar um pedido cancelado → 400; pagar um UUID inexistente → 404
- [X] T030 [US2] Rodar `mvn test`: verde

**Checkpoint**: US1 e US2 funcionam e são testáveis de forma independente

---

## Phase 5: User Story 3 - Impedir cancelamento de pedido já pago (Priority: P3)

**Goal**: `cancelar()` recusa pedido PAGO; o cancelamento de pedido CRIADO segue igual.

**Independent Test**: pagar um pedido e chamar `PATCH /pedidos/{id}/cancelamento` → 400 "Não é
possível cancelar um pedido pago", e o pedido continua PAGO.

### Tests for User Story 3 ⚠️

- [X] T031 [P] [US3] Em `test/domain/model/PedidoTest.java`, no `@Nested` existente "ao cancelar", adicionar "não pode cancelar pedido pago": `PedidoInvalidoException` "Não é possível cancelar um pedido pago", com status, forma e data inalterados. Manter os testes existentes do bloco
- [X] T032 [P] [US3] Em `test/application/service/CancelarPedidoServiceTest.java`, adicionar: pedido pago → `PedidoInvalidoException` propagada e `salvar` nunca chamado

### Implementation for User Story 3

- [X] T033 [US3] Em `main/domain/model/Pedido.java`, fazer `cancelar()` recusar status PAGO (data-model.md › `Pedido.cancelar`), mantendo a mensagem existente para CANCELADO. Faz T031 e T032 passarem
- [X] T034 [US3] Em `test/PedidoApiIntegrationTest.java`, adicionar o teste "não cancela pedido pago": criar, pagar, `PATCH {location}/cancelamento` → 400 com `$.detail` "Não é possível cancelar um pedido pago", e o `GET` continua com status PAGO
- [X] T035 [US3] Rodar `mvn test`: verde

**Checkpoint**: todas as histórias funcionam de forma independente

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T036 Revisar a conformidade com a constituição: nenhum import de Spring ou JPA em `main/domain/` e `main/application/`; `PagarPedidoService` sem `@Service`; nenhum `if` de regra de negócio em service ou controller; mensagens e `@DisplayName` em português
- [X] T037 Rodar `mvn test` e conferir no relatório do JaCoCo (`target/site/jacoco/index.html`) que `Pedido.pagar`, `FormaPagamento.de` e `PagarPedidoService` estão cobertos
- [X] T038 Executar a validação manual de `specs/001-pagamento-pedido/quickstart.md` (passos 1 a 7)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** → **Foundational (Phase 2)** → **US1 (Phase 3)** → **US2 (Phase 4)** e **US3 (Phase 5)** → **Polish (Phase 6)**
- Dentro da Phase 2, a T002 (mover as classes JPA) vem primeiro, antes de T007 e T015, que editam
  arquivos no novo pacote

### User Story Dependencies

- **US1 (P1)**: depende apenas da Phase 2
- **US2 (P2)**: depende de US1 (estende `pagar()`, o endpoint e o service criados em US1)
- **US3 (P3)**: o domínio (T031, T033) depende apenas da Phase 2 e de `pagar()` (T013) para montar o
  cenário; o teste ponta a ponta (T034) usa o endpoint de US1. Pode correr em paralelo com US2

### Within Each User Story

- Testes escritos e falhando → domínio → caso de uso → persistência → web → ponta a ponta → `mvn test`
- T013, T026 e T033 editam o mesmo arquivo (`Pedido.java`): executar em sequência
- T020, T029 e T034 editam `PedidoApiIntegrationTest.java`: executar em sequência

### Parallel Opportunities

- Phase 2 (após T002): T003 e T005
- US1: T009, T010, T011 e T012 (testes em arquivos distintos)
- US2: T022, T023, T024 e T025
- US3: T031 e T032; a fase toda em paralelo com US2, exceto pelos arquivos compartilhados acima

---

## Parallel Example: User Story 1

```bash
# Testes de US1 juntos (arquivos distintos):
Task: "T009 Nested 'ao pagar' em test/domain/model/PedidoTest.java"
Task: "T010 PagarPedidoServiceTest em test/application/service/"
Task: "T011 Pedido pago no PedidoJpaAdapterTest"
Task: "T012 POST /pedidos/{id}/pagamento no PedidoControllerTest"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 e Phase 2
2. Phase 3 (US1) → `mvn test` verde → validar os passos 1 e 2 do quickstart
3. **PARAR e VALIDAR**

### Incremental Delivery

1. Base + US1 → MVP (pagamento funcional)
2. + US2 → regras de recusa e erros padronizados
3. + US3 → ciclo de vida fechado (PAGO é estado final)
4. Polish → conformidade e cobertura

---

## Notes

- [P] = arquivos diferentes, sem dependência de tarefas incompletas
- Cada checkpoint exige `mvn test` verde (Constituição, Princípio III)
- Verificar que os testes falham antes de implementar
- Limitação conhecida: pagamentos simultâneos não são tratados (spec.md › Assumptions; research.md D6)

## Phase 7: Convergence

- [X] T039 [US2] Em `test/domain/model/PedidoTest.java`, no `@Nested` "ao pagar", escrever primeiro (devem falhar) os testes "não pode pagar pedido criado há mais de 7 dias", que monta o pedido com `Pedido.reconstruir(...)` e `criadoEm = Instant.now().minus(8, DAYS)`, espera `PedidoInvalidoException` "O prazo para pagamento deste pedido expirou" e verifica que o status continua CRIADO, com `formaPagamento` e `pagoEm` nulos; e "paga pedido criado há menos de 7 dias", com `criadoEm` 6 dias atrás, que passa a PAGO. Fonte: FR-013, US2/AC5 (missing)
- [X] T040 [US2] Em `main/domain/model/Pedido.java`, fazer `pagar(...)` recusar, depois das verificações de CANCELADO e PAGO e antes de alterar o estado, pedido cujo `criadoEm` seja anterior a `Instant.now()` menos 7 dias, com `PedidoInvalidoException("O prazo para pagamento deste pedido expirou")`. Guardar o prazo numa constante do domínio (`Duration.ofDays(7)`). Faz T039 passar. Fonte: FR-013, FR-010 (missing)
- [X] T041 [P] [US2] Em `test/application/service/PagarPedidoServiceTest.java`, adicionar: pedido expirado (via `Pedido.reconstruir` com `criadoEm` 8 dias atrás) propaga `PedidoInvalidoException` e `salvar` nunca é chamado. Fonte: FR-013, FR-010 (missing)
- [X] T042 [US2] Em `test/PedidoApiIntegrationTest.java`, adicionar o teste "recusa pagamento de pedido com prazo expirado": salvar via `PedidoRepositoryPort` (injetado com `@Autowired`) um pedido reconstruído com `criadoEm` 8 dias atrás, chamar `POST /pedidos/{id}/pagamento` e esperar 400 com `$.detail` "O prazo para pagamento deste pedido expirou", e o `GET` deve continuar CRIADO. Fonte: US2/AC5 (missing)
- [X] T043 [P] Atualizar `specs/001-pagamento-pedido/data-model.md` (regras de `Pedido.pagar`, incluindo o prazo de 7 dias depois de CANCELADO e PAGO) e `specs/001-pagamento-pedido/contracts/pagamento-api.md` (linha "Prazo expirado" na tabela de 400). Fonte: FR-013 (missing)
- [X] T044 Rodar `mvn clean test`: verde, com os 69 testes existentes mais os novos. Fonte: Constituição III (missing)
