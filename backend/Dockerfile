# Build
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /autoflow

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S autoflowuser -G appgroup

WORKDIR /autoflow

COPY --from=build --chown=autoflowuser:appgroup /autoflow/target/*.jar app.jar

USER autoflowuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]