# syntax=docker/dockerfile:1
# Multi-stage Dockerfile for Payment Orchestrator Backend

# Stage 1: Build stage
# Use a stable Maven image with JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POM and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application jar (skip tests in container build)
COPY src ./src
RUN mvn clean package -DskipTests -B
# Rename the fat jar (exclude the -plain.jar stub) to a known fixed name
RUN find /app/target -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -exec cp {} /app/target/app-exec.jar \;

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Add a non-root system user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

# Copy the renamed fat jar from build stage
COPY --from=build /app/target/app-exec.jar app.jar

# Render injects $PORT; Spring reads server.port=${PORT:8080} so this just works
EXPOSE 8080

# Configure JVM flags suitable for container memory constraints
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
