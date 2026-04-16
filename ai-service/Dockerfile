# ── Stage 1: Build ────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install Maven (not included in temurin alpine image)
RUN apk add --no-cache maven

WORKDIR /app

# Cache dependencies layer separately from source
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user
RUN addgroup -S payflow && adduser -S payflow -G payflow

COPY --from=builder /app/target/*.jar app.jar

USER payflow
EXPOSE 8087

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
