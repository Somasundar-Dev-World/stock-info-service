# ============================================================
# Stage 1: Build - Use Maven + JDK to compile and package the app
# ============================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and POM first (layer caching for dependencies)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached layer - only re-runs if pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B 2>/dev/null || true

# Copy source code
COPY src ./src

# Build the application (skip tests during image build; run them in CI)
RUN ./mvnw clean package -DskipTests -B

# Extract layered JAR for optimized image layers
RUN java -Djarmode=layertools -jar target/stock-info-service.jar extract --destination target/extracted

# ============================================================
# Stage 2: Runtime - Minimal JRE image for production
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

# Copy layered JAR content (ordering matters - most stable layers first)
COPY --from=builder /app/target/extracted/dependencies/ ./
COPY --from=builder /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/target/extracted/application/ ./

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check for Docker/Kubernetes
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM tuning for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

# Spring Boot entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
