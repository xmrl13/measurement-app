# Etapa 1: Build
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila a aplicação e gera o JAR
RUN mvn clean package -DskipTests -B

# Etapa 2: Runtime
FROM openjdk:21-ea-slim
WORKDIR /app

# Copia o JAR gerado na etapa de build
COPY --from=builder /app/target/eureka-0.0.1-SNAPSHOT.jar eureka-server.jar

EXPOSE 8761

CMD ["java", "-jar", "eureka-server.jar"]
