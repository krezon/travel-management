# Stage 1: Builder
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy dependency resolution files first for layer caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy source and build
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd --gid 1001 app && useradd --uid 1001 --gid 1001 --create-home --shell /bin/bash app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown app:app app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
