# Build
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /autoflow

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jdk

WORKDIR /autoflow

COPY --from=build /autoflow/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]