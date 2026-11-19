<!--
Relatório de Impacto da Sincronização (Sync Impact Report)
- Versão: (template não preenchido) → 1.0.0
- Princípios definidos (primeira ratificação):
  - [PRINCIPLE_1_NAME] → I. Arquitetura Hexagonal (NÃO NEGOCIÁVEL)
  - [PRINCIPLE_2_NAME] → II. Regras de Negócio no Domínio
  - [PRINCIPLE_3_NAME] → III. Testes Primeiro (NÃO NEGOCIÁVEL)
  - [PRINCIPLE_4_NAME] → IV. API REST Consistente
  - [PRINCIPLE_5_NAME] → V. Simplicidade
- Seções adicionadas: Stack e Restrições Técnicas; Fluxo de Desenvolvimento e Portões de Qualidade
- Seções removidas: nenhuma
- Templates: nenhum template versionado alterado (leem a constituição em tempo de execução)
- TODOs adiados: nenhum
- Observações sobre o código atual (não corrigidas aqui; apenas constatadas):
  - PedidoEntity e ItemPedidoEmbeddable (JPA) estão em application.port.out; pelo Princípio I
    deveriam residir em adapter.out.persistence.
  - PedidoController está em adapter.in.web.dto; o pacote esperado é adapter.in.web.
-->

# Constituição do Pedidos Hexagonal

## Princípios Fundamentais

### I. Arquitetura Hexagonal (NÃO NEGOCIÁVEL)

- O pacote `domain` DEVE ser Java puro: nenhuma importação de Spring, Jakarta Persistence (JPA)
  ou qualquer outro framework.
- Casos de uso DEVEM ser implementados em `application.service` e expostos por interfaces em
  `application.port.in`.
- Todo acesso a dados DEVE passar por portas (interfaces) em `application.port.out`; serviços
  NUNCA dependem diretamente de repositórios Spring Data ou entidades JPA.
- Controllers, DTOs web, entidades JPA, repositórios e mapeadores de persistência DEVEM ficar em
  `adapter` (`adapter.in.web`, `adapter.out.persistence`).
- Casos de uso DEVEM ser registrados como `@Bean` em `config.UseCaseConfig`. É PROIBIDO anotar
  serviços de aplicação com `@Service`, `@Component` ou equivalentes.

**Justificativa**: isolar o núcleo do negócio de frameworks mantém o domínio testável sem
contexto Spring e permite trocar adaptadores (web, persistência) sem tocar nas regras.

### II. Regras de Negócio no Domínio

- Validações, invariantes e transições de status DEVEM ficar em `domain.model`
  (ex.: `Pedido`, `ItemPedido`, `StatusPedido`).
- Violação de regra de negócio DEVE lançar `PedidoInvalidoException`.
- Pedido inexistente DEVE lançar `PedidoNaoEncontradoException`.
- Services DEVEM apenas orquestrar: carregar via porta de saída, delegar a decisão ao domínio,
  persistir via porta de saída. Nenhum `if` de regra de negócio em services ou controllers.

**Justificativa**: regras concentradas no modelo evitam duplicação e anemia do domínio, e cada
regra é verificável por teste unitário puro.

### III. Testes Primeiro (NÃO NEGOCIÁVEL)

- Toda funcionalidade nova DEVE ter testes escritos antes do código de produção, e esses testes
  DEVEM falhar antes da implementação (ciclo Vermelho → Verde → Refatorar).
- Camadas e ferramentas obrigatórias:
  - **Domínio**: JUnit 5 + AssertJ, organizados com `@Nested` e `@DisplayName` em português.
  - **Casos de uso**: Mockito, simulando as portas de saída.
  - **Controller**: `@WebMvcTest` com `@MockitoBean` para os casos de uso.
  - **Persistência**: `@DataJpaTest`.
  - **Fluxo completo**: `@SpringBootTest`.
- Os testes existentes DEVEM continuar passando; é proibido apagá-los ou desativá-los para
  "fazer passar".
- `mvn test` verde é OBRIGATÓRIO ao final de cada fase de implementação.

**Justificativa**: testes escritos antes definem o comportamento esperado e protegem contra
regressões em um código que evolui em fases.

### IV. API REST Consistente

- Recursos DEVEM ser nomeados no plural (ex.: `/pedidos`).
- Ações sobre um recurso DEVEM ser modeladas como sub-recursos
  (ex.: `POST /pedidos/{id}/cancelamento`), nunca como verbos na URL.
- Erros DEVEM ser retornados como `ProblemDetail` (RFC 9457), tratados centralmente no
  `GlobalExceptionHandler`:
  - `PedidoInvalidoException` → **400 Bad Request**;
  - `PedidoNaoEncontradoException` → **404 Not Found**.
- Controllers NÃO DEVEM montar respostas de erro manualmente.

**Justificativa**: contratos previsíveis simplificam clientes e testes de controller.

### V. Simplicidade

- Nenhuma dependência nova DEVE ser adicionada ao `pom.xml` sem justificativa explícita no
  `plan.md` da funcionalidade (seção de verificação da constituição / rastreamento de
  complexidade).
- Integrações externas reais (gateways de pagamento, filas, APIs de terceiros) são PROIBIDAS;
  quando necessárias, DEVEM ser representadas por uma porta de saída com adaptador simulado.
- Código (classes, métodos, variáveis), mensagens de exceção, `@DisplayName` e mensagens de
  erro da API DEVEM estar em português.
- Preferir a solução mais simples que atenda à especificação (YAGNI).

**Justificativa**: projeto didático e enxuto; cada peça adicional precisa pagar seu custo.

## Stack e Restrições Técnicas

- **Linguagem**: Java 20.
- **Framework**: Spring Boot 3.5 (web, Spring Data JPA).
- **Build**: Maven.
- **Banco de dados**: H2 (em memória).
- **Testes**: JUnit 5, AssertJ, Mockito (via `spring-boot-starter-test`).
- **Cobertura**: JaCoCo.
- Pacote raiz: `br.com.loja.pedidos`, com subpacotes `domain`, `application`, `adapter` e
  `config`.

## Fluxo de Desenvolvimento e Portões de Qualidade

- Cada funcionalidade segue o fluxo Spec Kit: especificação → plano → tarefas → implementação.
- O `plan.md` DEVE conter uma verificação explícita contra os cinco princípios; qualquer
  exceção DEVE ser registrada com justificativa e alternativa mais simples rejeitada.
- As tarefas DEVEM ordenar os testes antes do código correspondente em cada fase.
- Portão de cada fase: `mvn test` verde, sem testes desativados.
- Revisões de código DEVEM verificar: ausência de framework em `domain`, ausência de
  `@Service` em casos de uso, regras de negócio apenas no domínio e erros via
  `GlobalExceptionHandler`.

## Governança

- Esta constituição prevalece sobre quaisquer outras práticas do projeto.
- Emendas DEVEM ser feitas via `/speckit-constitution`, com Relatório de Impacto e atualização
  da versão e da data de última emenda.
- Versionamento semântico:
  - **MAJOR**: remoção ou redefinição incompatível de princípio ou regra de governança;
  - **MINOR**: novo princípio ou seção, ou expansão material de orientação;
  - **PATCH**: esclarecimentos, redação e correções sem efeito semântico.
- Toda revisão de plano, tarefa ou pull request DEVE verificar a conformidade com esta
  constituição; violações não justificadas bloqueiam a entrega.

**Versão**: 1.0.0 | **Ratificada**: 2026-11-19 | **Última emenda**: 2026-11-19
