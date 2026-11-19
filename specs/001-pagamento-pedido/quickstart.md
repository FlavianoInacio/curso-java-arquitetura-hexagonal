# Quickstart: validar o Pagamento de Pedido

## Pré-requisitos

- JDK 20 e Maven no PATH.

## 1. Testes automatizados

```bash
mvn test
```

Esperado: build verde, incluindo os testes já existentes e os novos de domínio, caso de uso,
controller, persistência e o fluxo ponta a ponta. O relatório do JaCoCo fica em
`target/site/jacoco/index.html`.

## 2. Validação manual

```bash
mvn spring-boot:run
```

1. Criar um pedido e anotar o `id` da resposta:

   ```bash
   curl -s -X POST localhost:8080/pedidos -H "Content-Type: application/json" \
     -d '{"cliente":"Maria","itens":[{"produto":"Teclado","quantidade":2,"precoUnitario":150.00}]}'
   ```

2. Pagar com PIX → 200, `status` = `PAGO`, `formaPagamento` = `PIX`, `pagoEm` preenchido:

   ```bash
   curl -s -X POST localhost:8080/pedidos/<id>/pagamento \
     -H "Content-Type: application/json" -d '{"formaPagamento":"PIX"}'
   ```

3. Pagar de novo → 400, `detail` = "O pedido já está pago".
4. Cancelar (`PATCH /pedidos/<id>/cancelamento`) → 400, "Não é possível cancelar um pedido pago".
5. Criar outro pedido, cancelá-lo e tentar pagar → 400, "Não é possível pagar um pedido cancelado".
6. Pagar um pedido com `{"formaPagamento":"DINHEIRO"}` → 400, "Forma de pagamento inválida: DINHEIRO".
7. Pagar um UUID inexistente → 404.

Detalhes do contrato: [contracts/pagamento-api.md](contracts/pagamento-api.md).
Regras e campos: [data-model.md](data-model.md).
