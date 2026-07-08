# ---------- Stage 1: build the jar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first so Maven can cache dependencies separately from source changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: run it on a small JRE ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/oms-backend-1.0.0.jar app.jar

# Render sets $PORT at runtime; application.properties should read it via
# server.port=${PORT:8080} for this to work on Render.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
