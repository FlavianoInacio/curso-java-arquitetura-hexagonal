# Contrato: Pagamento de Pedido

## POST /pedidos/{id}/pagamento

Paga o pedido `{id}` (UUID).

### Requisição

```json
{ "formaPagamento": "PIX" }
```

`formaPagamento`: obrigatório; um de `PIX`, `CARTAO_CREDITO`, `BOLETO`.

### Respostas

**200 OK**: pedido atualizado (`PedidoResponse`).

```json
{
  "id": "6f1c...",
  "cliente": "Maria",
  "status": "PAGO",
  "itens": [ { "produto": "Teclado", "quantidade": 2, "precoUnitario": 150.00, "subtotal": 300.00 } ],
  "total": 300.00,
  "criadoEm": "2026-11-19T12:00:00Z",
  "formaPagamento": "PIX",
  "pagoEm": "2026-11-19T12:05:00Z"
}
```

**400 Bad Request** (`application/problem+json`), título "Pedido Inválido":

| Situação | `detail` |
|----------|----------|
| Pedido CANCELADO | Não é possível pagar um pedido cancelado |
| Pedido já PAGO | O pedido já está pago |
| Prazo expirado (pedido CRIADO há mais de 7 dias) | O prazo para pagamento deste pedido expirou |
| `formaPagamento` ausente/vazio | A forma de pagamento é obrigatória |
| `formaPagamento` desconhecida | Forma de pagamento inválida: `<valor>` |

**400 Bad Request**, título "Requisição inválida", detail "O corpo da requisição é inválido": corpo
ausente ou JSON malformado.

**404 Not Found**, título "Pedido não encontrado!": pedido inexistente.

## Alterações em endpoints existentes

- `GET /pedidos/{id}`, `POST /pedidos` e `PATCH /pedidos/{id}/cancelamento`: o `PedidoResponse`
  ganha `formaPagamento` e `pagoEm` (nulos enquanto o pedido não está pago). Campos existentes
  inalterados.
- `PATCH /pedidos/{id}/cancelamento` em pedido PAGO → 400, detail
  "Não é possível cancelar um pedido pago".
