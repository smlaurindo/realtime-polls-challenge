FROM ghcr.io/graalvm/graalvm-community:25.0.0@sha256:36b3bff7b5a6956ab9b4bf5e7aa895022f7ed14da7b8275baa73ca1c34520397 AS builder
WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY src ./src

RUN chmod +x ./gradlew

RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre-alpine@sha256:b51543f89580c1ba70e441cfbc0cfc1635c3c16d2e2d77fec9d890342a3a8687 AS runtime

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

