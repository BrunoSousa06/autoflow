# Testes e qualidade

## Pre-condicoes

- Java 21
- Maven instalado no ambiente
- Node.js 20
- npm 10
- Chrome ou Chromium disponivel para os testes frontend headless

O repositorio nao possui `mvnw` ou `mvnw.cmd`. Ate que o wrapper seja criado, os comandos backend oficiais usam `mvn`.

## Testes unitarios

- JUnit 5
- Mockito
- AssertJ
- Nao iniciar o contexto Spring

## Testes de integracao

- Testcontainers
- PostgreSQL real em container
- Executar migrations Flyway

## Convencoes

Nome dos testes:

```text
metodo_quandoCondicao_deveResultado
```

## Validacoes

### Backend

```bash
cd backend
mvn test
mvn verify
```

Relatorios JaCoCo:

- HTML: `backend/target/site/jacoco/index.html`
- XML: `backend/target/site/jacoco/jacoco.xml`
- CSV: `backend/target/site/jacoco/jacoco.csv`

O `backend/pom.xml` aplica check minimo de 80% para linha e branch no `verify`.

Resultado validado em 07/08/2026:

| Metrica JaCoCo | Cobertura | Status |
|---|---:|---|
| Instructions | 87,25% (10.463/11.992) | OK |
| Branches | 80,03% (557/696) | OK |
| Lines | 89,19% (2.326/2.608) | OK |

Foram executados 703 testes backend, sem falhas. O relatorio HTML e o XML devem ser
consultados antes de alterar exclusoes ou thresholds. As exclusoes atuais sao somente
`SecurityConfig`, mappers de persistencia, gateways e a classe de bootstrap; o pacote
`domain` permanece incluido na metrica.

### Frontend

```bash
cd frontend
npm ci
npm run build
npm run test:ci
```

Relatorio de cobertura frontend:

- `frontend/coverage/frontend/index.html`
- `frontend/coverage/frontend/lcov.info`

Resultado validado em 07/08/2026: 369 testes, sem falhas; statements 86,52%, branches
66,74%, functions 85,24% e lines 87,43%. O frontend ainda nao possui quality gate de
branches; a metrica deve orientar os proximos cards sem ser mascarada por exclusoes.

### Sonar

A analise Sonar depende de ambiente configurado e do secret `SONAR_TOKEN`.

No CI, o job backend executa:

```bash
cd backend
mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=BrunoSousa06_autoflow -Dsonar.qualitygate.wait=true -Dsonar.sources=src/main/java -Dsonar.tests=src/test/java
```

## Fronteiras arquiteturais

`ArchitectureBoundaryTest` verifica que:

- controllers legados nao acessam repositories diretamente;
- presentation nao acessa repositories diretamente;
- repositories nao dependem de controllers;
- infrastructure nao cria novas dependencias para presentation/controller;
- application nao cria novas dependencias para infrastructure, presentation, controller,
  repository ou service.

As duas ultimas regras e as regras legadas de domain/service usam `FreezingArchRule`.
O store em `backend/archunit_store` registra somente as violacoes preexistentes; uma
violacao nova falha o teste. A migracao fisica de classes continua fora deste card.

O workflow efetivo esta em `.github/workflows/pipeline.yml`. O Sonar depende de
`SONAR_TOKEN` e de acesso ao servico remoto; sem esses dois pre-requisitos o status deve
ser reportado como bloqueado, nunca como sucesso. O CI publica os relatorios JaCoCo e de
cobertura frontend como artefatos quando forem gerados.

## Definition of Done para cards de qualidade/refatoracao

- testes focados e suite do modulo executados;
- `mvn verify`, `npm run test:ci` e `npm run build` executados quando o card tocar as areas;
- cobertura reportada com numeros e caminhos dos relatorios;
- nenhuma exclusao ampla ou reducao de threshold para obter build verde;
- ArchitectureBoundaryTest aprovado e novas dividas registradas no store/backlog;
- Sonar marcado como aprovado ou bloqueado por pre-condicao objetiva;
- checklist SOLID revisado e contratos REST/migrations confirmados como inalterados.
