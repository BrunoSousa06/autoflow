# Cobertura backend - baseline

## Configuracao JaCoCo

- Plugin: `jacoco-maven-plugin` 0.8.12.
- Escopo: bundle completo.
- Gate: minimo de 80% para `LINE` e `BRANCH` no `mvn verify`.
- Relatorios: `backend/target/site/jacoco/index.html`, `jacoco.xml` e `jacoco.csv`.

O pacote `com/autoflow/domain/**` permanece incluido. As exclusoes atuais sao limitadas
a `SecurityConfig`, mappers de persistencia, gateways e a classe de bootstrap.

## Medicao atual

Executado em 07/08/2026 com `cd backend; mvn verify`.

| Metrica | Coberto | Total | Cobertura | Meta | Status |
|---|---:|---:|---:|---:|---|
| Instructions | 10.463 | 11.992 | 87,25% | - | OK |
| Branches | 557 | 696 | 80,03% | 80% | OK |
| Lines | 2.326 | 2.608 | 89,19% | 80% | OK |

A suite executou 703 testes, sem falhas. Os cenarios adicionados nesta rodada cobrem
branches reais de estoque, atribuicao de mecanico, inclusao de servicos, politica de
acesso, validacao de documentos e mensagens de status.

## Fronteiras arquiteturais

`ArchitectureBoundaryTest` passou com oito regras. As regras que ainda encontram dividas
legadas usam `FreezingArchRule` e os arquivos do store ficam em
`backend/archunit_store`. O store nao e uma exclusao de cobertura: uma dependencia nova
nas fronteiras congeladas falha o teste.

## Proximos passos

1. Migrar componentes individualmente conforme a ADR-001.
2. Remover entradas do store somente junto com a correcao arquitetural correspondente.
3. Cobrir os branches restantes de OS e orcamento em cards de componente, sem alterar o
   threshold global.
