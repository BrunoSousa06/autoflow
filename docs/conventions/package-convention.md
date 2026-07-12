# Convenção de pacotes — Clean Architecture

Referência: [ADR-001](../adr/ADR-001-sequencia-migracao-clean-architecture.md)

## Estrutura alvo

```
com.autoflow
├── domain
│   └── <componente>          # Entidades, value objects, enums, exceções de domínio
│                             # Regra: sem Spring, JPA, HTTP ou DTO REST
│
├── application
│   └── <componente>          # Use cases, commands, queries, DTOs internos
│   └── gateway               # Interfaces de acesso a dados e serviços externos
│                             # Regra: depende apenas de domain e de abstrações
│
├── infrastructure
│   └── persistence
│       └── <componente>      # Entities JPA, adapters de repositório
│       └── repository        # Spring Data repositories concretos
│   └── security              # JwtFilter, SecurityConfig, CustomUserDetailsService
│   └── notification          # Adapters de e-mail, PDF
│
└── presentation
    └── rest
        └── <componente>      # Controllers, requests, responses REST
                              # Regra: converte entrada/saída, sem lógica de negócio
```

## Estado atual (julho 2026)

O código ainda usa a estrutura técnica legada. A migração ocorre por componente, conforme ADR-001.

| Pacote legado | Camada alvo | Status |
|---|---|---|
| `com.autoflow.controller` | `presentation.rest` | Legado — migrar por componente |
| `com.autoflow.service` | `application` | Legado — migrar por componente |
| `com.autoflow.repository` | `infrastructure.persistence.repository` | Legado — migrar por componente |
| `com.autoflow.domain` | `domain` (puro, sem JPA) | Legado — entidades ainda têm `@Entity` |
| `com.autoflow.mapper` | `infrastructure.persistence` ou `presentation.rest` | Legado |
| `com.autoflow.handler` | `presentation.rest` | Legado |
| `com.autoflow.config.security` | `infrastructure.security` | Legado |

## Regras de dependência

| Camada | Pode depender de | Não pode depender de |
|---|---|---|
| `domain` | Nada externo ao domínio | Spring, JPA, HTTP, mensageria, DTO REST |
| `application` | `domain`, interfaces de `gateway` | Adapters concretos de `infrastructure` |
| `infrastructure` | `domain`, `application.gateway`, Spring, JPA | `presentation` |
| `presentation.rest` | `application`, `domain` (apenas leitura) | `infrastructure` diretamente |

## Violações conhecidas (rastreadas pelo ArchUnit)

| Violação | Arquivo | Gravidade | Card de correção |
|---|---|---|---|
| `OrdemServicoEntity` importa `ResponseStatusException` (Spring Web) | `domain/ordemservico/OrdemServicoEntity.java` | Alta | A criar — migração de OS |
| Entidades de domínio usam `@Entity`, `@Table` etc. (JPA no domínio) | Todos os `*Entity.java` em `domain/` | Alta | Cada card de componente |
| `OrdemServicoService` (interface) importa tipos de `controller/` | `service/ordemservico/OrdemServicoService.java` | Alta | A criar — migração de OS |

## Critério de entrada para mover um componente

Antes de mover fisicamente qualquer classe, o componente deve ter:

1. Contrato REST caracterizado por testes de integração existentes;
2. Testes cobrindo sucesso e falhas relevantes;
3. Dependências mapeadas;
4. Decisão explícita de package registrada (este documento ou ADR);
5. Nenhuma alteração de contrato REST como efeito colateral.

## Convenção de nomes

| Artefato | Sufixo | Exemplo |
|---|---|---|
| Use case | `UseCase` | `CriarServicoUseCase` |
| Gateway (interface) | `Gateway` | `ServicoGateway` |
| Adapter de persistência | `PersistenceAdapter` | `ServicoPersistenceAdapter` |
| Entity JPA | `JpaEntity` ou `Entity` (legado) | `ServicoJpaEntity` |
| Controller REST | `Controller` | `ServicoController` |
| Request REST | `Request` | `ServicoRequest` |
| Response REST | `Response` | `ServicoResponse` |