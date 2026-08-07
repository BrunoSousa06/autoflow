# Checklist SOLID para revisão de PRs de refatoração

Use este checklist ao revisar qualquer card de refatoração do backlog de Clean Architecture.

## S — Single Responsibility Principle

- [ ] A classe tem uma única razão para mudar?
- [ ] Um controller faz somente conversão de entrada/saída e delegação ao use case?
- [ ] Um use case orquestra uma única operação do ponto de vista do domínio?
- [ ] Um repository ou adapter é responsável por uma única entidade ou agregado?
- [ ] Não há métodos `criarOuAtualizar`, `buscarEProcessar` ou similares que misturam operações distintas?

**Sinais de violação no AutoFlow:**
- `OrdemServicoService` com dezenas de métodos que cobrem abertura, diagnóstico, orçamento, execução e entrega ao mesmo tempo.
- Controllers que chamam múltiplos services para montar uma única resposta.

---

## O — Open/Closed Principle

- [ ] Novos comportamentos podem ser adicionados sem modificar código existente?
- [ ] Condicionais do tipo `if (tipo == X) ... else if (tipo == Y)` foram substituídas por polimorfismo quando aplicável?
- [ ] Extensões de comportamento usam interfaces ou abstrações em vez de modificar a classe base?

**Sinais de violação no AutoFlow:**
- Lógica de notificação hardcoded dentro do service de OS em vez de delegar a um `NotificacaoGateway`.
- Lógica de geração de PDF acoplada ao service de orçamento em vez de ser uma estratégia substituível.

---

## L — Liskov Substitution Principle

- [ ] Implementações concretas honram o contrato definido pela interface ou classe base?
- [ ] Uma implementação concreta nunca lança exceção onde a interface promete sucesso?
- [ ] Um subtipo não restringe pré-condições além do que o contrato estabelece?

**Sinais de violação no AutoFlow:**
- Implementações de `OrdemServicoService` que adicionam pré-condições de segurança não documentadas na interface.
- Adapters que retornam `Optional.empty()` onde o gateway declara retorno obrigatório.

---

## I — Interface Segregation Principle

- [ ] Interfaces de gateway têm somente os métodos que o use case realmente usa?
- [ ] Nenhum use case é forçado a depender de métodos que nunca chama?
- [ ] Interfaces de leitura e escrita estão separadas quando os consumidores são distintos?

**Sinais de violação no AutoFlow:**
- `OrdemServicoService` (interface) expõe ~20 métodos; use cases individuais precisariam de apenas 2-3.
- Repositories com métodos de busca complexa sendo injetados em use cases que só precisam de `salvar()`.

---

## D — Dependency Inversion Principle

- [ ] Use cases dependem de interfaces (gateways/ports), não de classes concretas de infraestrutura?
- [ ] O domínio não importa Spring, JPA, ou qualquer framework?
- [ ] As dependências entre módulos apontam de fora para dentro (infraestrutura → aplicação → domínio)?
- [ ] Nenhum use case instancia diretamente um repositório, client HTTP ou serviço externo?

**Sinais de violação no AutoFlow:**
- `OrdemServicoService` injeta `ServicoRepository` (Spring Data) diretamente.
- `OrdemServicoEntity` importa `ResponseStatusException` do Spring Web.
- Services conhecem tipos de `controller/` (request e response REST).

---

## Como aplicar durante a revisão

1. Para cada classe nova ou modificada no PR, marque cada item relevante.
2. Violações encontradas em código PREEXISTENTE ao escopo do card → registrar no backlog, não bloquear o PR.
3. Violações INTRODUZIDAS pelo PR → exigir correção antes de aprovação.
4. Em caso de dúvida, prefira a solução mais simples compatível com os princípios em vez de sobre-engenharia.

## Referências

- [ADR-001 — Sequência de migração para Clean Architecture](../adr/ADR-001-sequencia-migracao-clean-architecture.md)
- [Convenção de pacotes](../conventions/package-convention.md)
- [ArchitectureBoundaryTest](../../backend/src/test/java/com/autoflow/architecture/ArchitectureBoundaryTest.java)

## Definition of Done do card

- [ ] Testes focados e suite afetada foram executados e registrados.
- [ ] Cobertura de linhas e branches foi medida sem exclusoes amplas novas.
- [ ] `ArchitectureBoundaryTest` passou; qualquer divida preexistente permanece rastreada.
- [ ] O contrato REST, as roles, o schema e as migrations foram confirmados sem alteracao.
- [ ] Sonar foi executado ou ficou explicitamente bloqueado por `SONAR_TOKEN`/ambiente.
- [ ] Relatorios e comandos reproduziveis estao documentados.
- [ ] O card possui pendencias, riscos e proximo passo verificavel.
