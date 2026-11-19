# Data Model: Pagamento de Pedido

## FormaPagamento (novo, `domain.model`)

Enum com valores fechados:

| Valor | Descrição |
|-------|-----------|
| `PIX` | Pagamento via PIX |
| `CARTAO_CREDITO` | Cartão de crédito |
| `BOLETO` | Boleto bancário |

- `FormaPagamento.de(String valor)`: converte o texto recebido; nulo ou em branco (vazio ou só espaços) →
  `PedidoInvalidoException("A forma de pagamento é obrigatória")`; valor desconhecido →
  `PedidoInvalidoException("Forma de pagamento inválida: <valor>")`. A comparação é exata
  (maiúsculas, nome do enum).

## StatusPedido (alterado, `domain.model`)

`CRIADO`, `PAGO` (novo), `CANCELADO`.

## Pedido (alterado, `domain.model`)

| Campo | Tipo | Regra |
|-------|------|-------|
| `id` | UUID | inalterado |
| `cliente` | String | inalterado |
| `criadoEm` | Instant | inalterado |
| `itens` | List<ItemPedido> | inalterado |
| `status` | StatusPedido | agora pode ser `PAGO` |
| `formaPagamento` | FormaPagamento | **novo**; nulo enquanto não pago |
| `pagoEm` | Instant | **novo**; nulo enquanto não pago |

### Comportamentos

- `pagar(FormaPagamento forma)` (novo):
  - `forma == null` → `PedidoInvalidoException("A forma de pagamento é obrigatória")`
  - `status == CANCELADO` → `PedidoInvalidoException("Não é possível pagar um pedido cancelado")`
  - `status == PAGO` → `PedidoInvalidoException("O pedido já está pago")`
  - caso contrário: `status = PAGO`, `formaPagamento = forma`, `pagoEm = Instant.now()`
- `cancelar()` (alterado):
  - `status == CANCELADO` → `PedidoInvalidoException("O pedido já está cancelado")` (inalterado)
  - `status == PAGO` → `PedidoInvalidoException("Não é possível cancelar um pedido pago")` (novo)
  - caso contrário: `status = CANCELADO`
- `reconstruir(id, cliente, itens, status, criadoEm, formaPagamento, pagoEm)`: ganha os dois
  parâmetros finais (usado apenas pelo mapper de persistência).
- Exceções não alteram nenhum campo do pedido (FR-010).

### Transições de status

```text
CRIADO ──pagar()────▶ PAGO        (final)
CRIADO ──cancelar()─▶ CANCELADO   (final)
```

## PedidoEntity (alterado, persistência; tabela `pedidos`)

Movida, junto com `ItemPedidoEmbeddable`, de `application.port.out` para
`adapter.out.persistence` (Constituição, Princípio I).

| Coluna | Tipo | Nulo |
|--------|------|------|
| `forma_pagamento` | VARCHAR (enum STRING) | sim |
| `pago_em` | TIMESTAMP | sim |

Sem tabela nova. Colunas criadas pelo `ddl-auto: create-drop`.
