# --- Build ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia apenas o pom e as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria um usuário não root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o JAR gerado do build
COPY --from=build /app/target/*.jar app.jar

# Cnfigurações de execução
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]