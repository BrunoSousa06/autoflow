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
mvn clean verify
```

Relatorios JaCoCo:

- HTML: `backend/target/site/jacoco/index.html`
- XML: `backend/target/site/jacoco/jacoco.xml`
- CSV: `backend/target/site/jacoco/jacoco.csv`

O `backend/pom.xml` aplica check minimo de 80% para linha e branch no `verify`.

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

### Sonar

A analise Sonar depende de ambiente configurado e do secret `SONAR_TOKEN`.

No CI, o job backend executa:

```bash
cd backend
mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=BrunoSousa06_autoflow -Dsonar.qualitygate.wait=true -Dsonar.sources=src/main/java -Dsonar.tests=src/test/java
```