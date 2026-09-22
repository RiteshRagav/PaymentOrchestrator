# syntax=docker/dockerfile:1
# Multi-stage Dockerfile for Payment Orchestrator Backend

# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy POM and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application jar (skip tests in container build)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Add a non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact from build stage
COPY --from=build /app/target/payment-orchestrator-backend-*.jar app.jar

# Expose standard application port
EXPOSE 8080

# Configure JVM flags suitable for container memory constraints
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
