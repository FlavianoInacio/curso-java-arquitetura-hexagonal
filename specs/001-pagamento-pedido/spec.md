# Feature Specification: Pagamento de Pedido

**Feature Branch**: `feature/pagamento`

**Created**: 2026-11-19

**Status**: Draft

**Input**: User description: "Quero permitir que o cliente pague um pedido. O pagamento pode ser feito por PIX, cartão de crédito ou boleto. Depois de pago, o pedido muda para o status PAGO e guarda a forma de pagamento e a data do pagamento. Não pode pagar um pedido cancelado nem pagar duas vezes o mesmo pedido."

## Clarifications

### Session 2026-11-19

- Q: Um pedido que já foi pago (status PAGO) pode ser cancelado? → A: Não. O cancelamento é recusado
  como regra violada ("Não é possível cancelar um pedido pago") e o pedido continua PAGO.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Pagar um pedido em aberto (Priority: P1)

O cliente tem um pedido recém-criado e quer quitá-lo escolhendo uma das formas de pagamento
aceitas: PIX, cartão de crédito ou boleto. Após o pagamento, o pedido passa a constar como pago,
com a forma de pagamento escolhida e a data em que o pagamento foi registrado.

**Why this priority**: é o fluxo principal da funcionalidade; sem ele não há valor entregue.

**Independent Test**: criar um pedido, pagá-lo com cada uma das três formas e consultar o pedido,
verificando status PAGO, forma de pagamento e data do pagamento.

**Acceptance Scenarios**:

1. **Given** um pedido com status CRIADO, **When** o cliente paga com PIX, **Then** o pedido passa
   ao status PAGO, registra a forma de pagamento PIX e a data/hora do pagamento.
2. **Given** um pedido com status CRIADO, **When** o cliente paga com cartão de crédito, **Then** o
   pedido passa ao status PAGO com forma de pagamento CARTÃO DE CRÉDITO e data do pagamento.
3. **Given** um pedido com status CRIADO, **When** o cliente paga com boleto, **Then** o pedido
   passa ao status PAGO com forma de pagamento BOLETO e data do pagamento.
4. **Given** um pedido pago, **When** o cliente consulta o pedido, **Then** vê o status PAGO, a
   forma de pagamento e a data do pagamento.

---

### User Story 2 - Impedir pagamentos inválidos (Priority: P2)

O sistema recusa tentativas de pagamento que violam as regras do negócio: pagar um pedido
cancelado, pagar novamente um pedido já pago, informar forma de pagamento não aceita ou pagar um
pedido que não existe. O cliente recebe uma mensagem clara explicando o motivo.

**Why this priority**: protege a integridade financeira (sem cobrança dupla nem cobrança de pedido
cancelado), mas depende do fluxo principal existir.

**Independent Test**: tentar pagar um pedido cancelado, um pedido já pago, um pedido inexistente e
um pedido sem forma de pagamento (ou com forma não aceita), verificando a recusa e que o pedido
permanece inalterado.

**Acceptance Scenarios**:

1. **Given** um pedido com status CANCELADO, **When** o cliente tenta pagá-lo, **Then** o pagamento
   é recusado como regra violada, com a mensagem "Não é possível pagar um pedido cancelado", e o
   pedido continua CANCELADO.
2. **Given** um pedido com status PAGO, **When** o cliente tenta pagá-lo novamente (com qualquer
   forma), **Then** o pagamento é recusado como regra violada, com a mensagem "O pedido já está
   pago", e a forma e a data do pagamento original permanecem inalteradas.
3. **Given** um identificador de pedido que não existe, **When** o cliente tenta pagá-lo, **Then** a
   operação é recusada informando que o pedido não foi encontrado.
4. **Given** um pedido com status CRIADO, **When** o cliente tenta pagá-lo sem informar a forma de
   pagamento ou com uma forma não aceita, **Then** o pagamento é recusado e o pedido continua CRIADO.

---

### User Story 3 - Impedir cancelamento de pedido já pago (Priority: P3)

Hoje um pedido pode ser cancelado em qualquer status que não seja CANCELADO. Com a introdução do
status PAGO, um pedido pago não pode mais ser cancelado, já que estorno está fora do escopo.

**Why this priority**: afeta a coerência do ciclo de vida do pedido, mas não bloqueia o pagamento.

**Independent Test**: pagar um pedido e tentar cancelá-lo, verificando a recusa e que o pedido
permanece PAGO com forma e data do pagamento inalteradas.

**Acceptance Scenarios**:

1. **Given** um pedido com status PAGO, **When** o cliente tenta cancelá-lo, **Then** o
   cancelamento é recusado como regra violada, com a mensagem "Não é possível cancelar um pedido
   pago", e o pedido continua PAGO com forma e data do pagamento inalteradas.
2. **Given** um pedido com status CRIADO, **When** o cliente o cancela, **Then** o pedido passa a
   CANCELADO, como já ocorre hoje.

---

### Edge Cases

- Forma de pagamento informada com grafia diferente das aceitas (ex.: "dinheiro"): recusada como
  dado inválido.
- Pedido criado, cancelado e depois tentado pagar: recusado (Story 2, cenário 1).
- Consulta de pedido ainda não pago: forma e data de pagamento aparecem vazias.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE permitir que um pedido com status CRIADO seja pago.
- **FR-002**: O sistema DEVE aceitar exatamente três formas de pagamento: PIX, CARTÃO DE CRÉDITO e
  BOLETO.
- **FR-003**: Ao concluir o pagamento, o sistema DEVE alterar o status do pedido para PAGO.
- **FR-004**: Ao concluir o pagamento, o sistema DEVE registrar a forma de pagamento escolhida e a
  data/hora em que o pagamento foi registrado (definida pelo sistema, não pelo cliente).
- **FR-005**: O sistema DEVE recusar o pagamento de pedido com status CANCELADO, tratando como
  regra de negócio violada.
- **FR-006**: O sistema DEVE recusar o pagamento de pedido com status PAGO, tratando como regra de
  negócio violada, sem alterar forma nem data do pagamento original.
- **FR-007**: O sistema DEVE recusar o pagamento de pedido inexistente, informando que o pedido não
  foi encontrado.
- **FR-008**: O sistema DEVE recusar pagamento sem forma de pagamento ou com forma não aceita.
- **FR-009**: A consulta de pedido DEVE exibir a forma de pagamento e a data do pagamento quando o
  pedido estiver pago, e deixá-las vazias caso contrário.
- **FR-010**: Pagamentos recusados NÃO DEVEM alterar nenhum dado do pedido.
- **FR-011**: Todas as mensagens apresentadas ao cliente DEVEM estar em português.
- **FR-012**: O sistema DEVE recusar o cancelamento de pedido com status PAGO, tratando como regra
  de negócio violada ("Não é possível cancelar um pedido pago"), sem alterar nenhum dado do pedido.

### Key Entities

- **Pedido**: já existente; passa a ter o status adicional PAGO e, quando pago, a forma de pagamento
  e a data do pagamento.
- **Forma de Pagamento**: conjunto fechado de opções aceitas (PIX, CARTÃO DE CRÉDITO, BOLETO).
- **Status do Pedido**: passa a ser CRIADO, PAGO ou CANCELADO. Transições permitidas apenas:
  CRIADO → PAGO e CRIADO → CANCELADO. PAGO e CANCELADO são estados finais.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das tentativas de pagamento de pedidos CRIADOS com forma aceita resultam em
  pedido PAGO com forma e data registradas.
- **SC-002**: 0 pedidos com mais de um pagamento registrado em tentativas sequenciais.
- **SC-003**: 0 pedidos cancelados que passem a constar como pagos, e 0 pedidos pagos que passem a
  constar como cancelados.
- **SC-004**: 100% das recusas apresentam ao cliente uma mensagem em português que identifica o
  motivo (pedido cancelado, já pago, inexistente ou forma inválida).
- **SC-005**: O cliente conclui o pagamento de um pedido em uma única ação, informando apenas o
  pedido e a forma de pagamento.

## Assumptions

- O pagamento é sempre do valor total do pedido; não há pagamento parcial, parcelamento, troco nem
  combinação de formas de pagamento.
- O pagamento é registrado como aprovado imediatamente: não há integração real com operadoras de
  cartão, PIX ou emissão de boleto, nem status intermediário "aguardando pagamento" (boleto
  incluso).
- Não são coletados dados sensíveis (número de cartão, chave PIX, linha digitável).
- Não há autenticação: quem conhece o identificador do pedido pode pagá-lo, como já ocorre com
  consulta e cancelamento.
- Estorno e reembolso estão fora do escopo.
- A data do pagamento é o instante em que o sistema registra o pagamento.
- Tentativas de pagamento simultâneas para o mesmo pedido não são tratadas nesta entrega
  (instância única, banco em memória); evolução futura.
