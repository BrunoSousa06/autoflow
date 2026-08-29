# Checklist SOLID para revisão de código

Use este checklist em mudanças de domínio, casos de uso, adapters, controllers e componentes frontend. Problemas
preexistentes devem ser registrados separadamente; a revisão deve bloquear apenas violações introduzidas pelo change.

## S — Single Responsibility

- [ ] A classe tem uma razão principal para mudar?
- [ ] O controller apenas valida/adapta HTTP e delega?
- [ ] O use case coordena uma operação coesa?
- [ ] O adapter trata uma integração, persistência ou conversão específica?
- [ ] A classe não mistura regra de negócio, persistência, notificação e apresentação?

Sinais de atenção no AutoFlow: use cases de OS que acumulam vários fluxos, controllers que montam regras e adapters que
fazem validação de domínio.

## O — Open/Closed

- [ ] Novo comportamento pode ser adicionado por uma porta, estratégia ou composição adequada?
- [ ] Condicionais por tipo ou perfil não cresceram sem necessidade?
- [ ] Extensões não exigem alterar uma classe base estável?

Não substitua polimorfismo simples por uma hierarquia excessiva. A solução deve continuar compatível com os estados e
perfis existentes.

## L — Liskov Substitution

- [ ] Implementações respeitam o contrato de suas interfaces?
- [ ] Adapters mantêm os mesmos significados de ausência, erro e transação?
- [ ] A implementação não adiciona pré-condições que o contrato não informa?
- [ ] Retornos e exceções permanecem compatíveis com os consumidores?

## I — Interface Segregation

- [ ] Cada gateway expõe somente operações usadas pelos consumidores?
- [ ] Portas de leitura e escrita estão separadas quando houver responsabilidades distintas?
- [ ] Um use case não depende de métodos que não utiliza?

No backend, prefira portas em `application/port/in` e `application/gateway` a fachadas com dezenas de operações.

## D — Dependency Inversion

- [ ] Use cases dependem de gateways, e não de repositories Spring Data?
- [ ] O domínio permanece livre de Spring, JPA, HTTP, Lombok e infraestrutura?
- [ ] Presentation não acessa adapters ou entidades JPA diretamente?
- [ ] Infrastructure implementa portas definidas internamente?
- [ ] Nenhum caso de uso instancia client, repository ou serviço externo?

## Compatibilidade e segurança

- [ ] Endpoints, payloads, status HTTP, autorização e mensagens foram preservados ou documentados?
- [ ] Fluxos públicos por token continuam protegidos contra token inválido, expirado ou conflitante?
- [ ] Transações, concorrência, auditoria e migrations permanecem corretas?
- [ ] Nenhum segredo, token ou dado pessoal foi adicionado a logs, testes ou documentação?

## Testes e arquitetura

- [ ] Há testes de sucesso, falha, autorização e limites para o comportamento alterado?
- [ ] A suíte de apresentação valida contrato HTTP quando aplicável?
- [ ] A suíte de integração cobre persistência e migrations quando aplicável?
- [ ] O teste [
  `ArchitectureBoundaryTest`](../../backend/src/test/java/com/autoflow/architecture/ArchitectureBoundaryTest.java)
  continua passando?
- [ ] `mvn clean verify` ou `npm run test:ci` foi executado conforme a camada alterada?

## Referências

- [Arquitetura](../architecture.md)
- [Convenção de pacotes](../conventions/package-convention.md)
- [ADR-001](../adr/ADR-001-sequencia-migracao-clean-architecture.md)
- [Testes e qualidade](../testing-and-quality.md)
