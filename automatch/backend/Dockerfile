# ============================================================
# Backend AutoMatch — build multi-stage
#   1) Maven compila y empaqueta el jar (tests se ejecutan en CI, no aqui)
#   2) Imagen final ligera solo con el JRE 21 y el jar
# ============================================================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom: la descarga de dependencias queda cacheada entre builds
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Las fotos subidas viven en /app/uploads (montar volumen en docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
