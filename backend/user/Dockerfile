# Etapa 1: Build da aplicação
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Runtime da aplicação
FROM openjdk:21-ea-slim
WORKDIR /app
COPY --from=builder /app/target/user-0.0.1-SNAPSHOT.jar user.jar
EXPOSE 8081
CMD ["java", "-jar", "user.jar"]
