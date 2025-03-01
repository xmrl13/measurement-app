# Etapa 1: Construção da aplicação
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Compila a aplicação e gera o JAR
RUN mvn clean package -DskipTests -B

# Etapa 2: Executando a aplicação
FROM openjdk:21-ea-slim
WORKDIR /app

# Copia o JAR gerado na etapa de build
COPY --from=builder /app/target/gateway-0.0.1-SNAPSHOT.jar gateway.jar

# Expõe a porta do Gateway
EXPOSE 8080

# Comando para rodar o serviço
CMD ["java", "-jar", "gateway.jar"]
