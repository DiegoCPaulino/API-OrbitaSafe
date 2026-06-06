# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Estágio 1 — build do JAR Quarkus (modo JVM / fast-jar)
# Usa uma imagem com Maven + JDK 21 só para compilar e empacotar.
# Nada deste estágio vai para a imagem final.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom primeiro (camada cacheável) e depois o código-fonte.
COPY pom.xml .
COPY src ./src

# Empacota sem rodar os testes (mesmo comando do deploy em modo JVM).
# Gera target/quarkus-app/ com quarkus-run.jar + lib/ + app/ + quarkus/.
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Estágio 2 — imagem de execução (apenas JRE 21 + artefato)
# Imagem final pequena: não carrega Maven nem o código-fonte.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /deployments

# Estrutura fast-jar do Quarkus, copiada em camadas (lib muda menos que app).
COPY --from=build /app/target/quarkus-app/lib/     ./lib/
COPY --from=build /app/target/quarkus-app/*.jar    ./
COPY --from=build /app/target/quarkus-app/app/     ./app/
COPY --from=build /app/target/quarkus-app/quarkus/ ./quarkus/

# Informativo: a API lê a porta de ${PORT:8080}. O Render injeta PORT
# automaticamente em runtime; o Quarkus já bind em 0.0.0.0 por padrão.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
