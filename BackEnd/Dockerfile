# Etapa 1: compila o projeto com Maven e Java 21 (gera target/cti-provedor-api.jar)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: imagem leve só com o Java para rodar o .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/cti-provedor-api.jar app.jar
EXPOSE 8080
# A porta vem da variável PORT (o Render define sozinho); a memória fica limitada ao plano grátis
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]