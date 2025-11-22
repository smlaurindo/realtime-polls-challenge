FROM ghcr.io/graalvm/graalvm-community:25.0.0@sha256:36b3bff7b5a6956ab9b4bf5e7aa895022f7ed14da7b8275baa73ca1c34520397 AS builder
WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY src ./src

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

#FROM eclipse-temurin:25-jdk AS builder
#WORKDIR /workspace
#
#COPY gradle ./gradle
#COPY gradlew .
#COPY settings.gradle.kts .
#COPY build.gradle.kts .
#COPY buildSrc ./buildSrc
#
#RUN --mount=type=cache,target=/root/.gradle \
#    --mount=type=cache,target=/root/.m2 \
#    ./gradlew --version
#
#COPY src ./src
#
#RUN --mount=type=cache,target=/root/.gradle \
#    ./gradlew clean test bootJar --no-daemon -x javadoc \
#
#RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination=dependency
#
#FROM eclipse-temurin:25-jre-jammy AS runtime
#WORKDIR /app
#
## Cria user não-root
#RUN addgroup --system spring && adduser --system --ingroup spring spring
#
## Copia as camadas separadamente para maximizar cache durante deploys (dependencies mudam menos que app)
#COPY --from=builder /workspace/dependency/spring-boot-loader/ ./spring-boot-loader/
#COPY --from=builder /workspace/dependency/dependencies/ ./dependencies/
#COPY --from=builder /workspace/dependency/snapshot-dependencies/ ./snapshot-dependencies/
#COPY --from=builder /workspace/dependency/application/ ./application/
#
## Ajusta permissões e usuário
#RUN chown -R spring:spring /app
#USER spring
#
## Expose padrão (ajusta se diferente)
#EXPOSE 8080
#
## Variáveis recomendadas: passe via runtime (secrets não no Dockerfile)
#ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
#
## Entrypoint: usa o Spring Boot JarLauncher que trabalha com as camadas
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp \"spring-boot-loader/*:dependencies/*:application/*\" org.springframework.boot.loader.JarLauncher"]
#
#HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
#  CMD curl -f http://localhost:8080/actuator/health || exit 1



# Multi-stage build for optimized production image
#FROM eclipse-temurin:25-jdk-alpine AS builder
#
#WORKDIR /app
#
## Copy gradle wrapper and build files
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle.kts .
#COPY settings.gradle.kts .
#
## Download dependencies (cached layer)
#RUN ./gradlew dependencies --no-daemon || true
#
## Copy source code
#COPY src src
#
## Build the application
#RUN ./gradlew bootJar --no-daemon -x test
#
## Production stage
#FROM eclipse-temurin:25-jre-alpine
#
#WORKDIR /app
#
## Create non-root user for security
#RUN addgroup -g 1001 -S appuser && \
#    adduser -u 1001 -S appuser -G appuser
#
## Copy jar from builder
#COPY --from=builder /app/build/libs/*.jar app.jar
#
## Change ownership to non-root user
#RUN chown -R appuser:appuser /app
#
## Switch to non-root user
#USER appuser
#
## Expose port
#EXPOSE 8080
#
## Health check
#HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
#
## JVM optimization for containers
#ENV JAVA_OPTS="-XX:+UseContainerSupport \
#    -XX:MaxRAMPercentage=75.0 \
#    -XX:InitialRAMPercentage=50.0 \
#    -XX:+UseG1GC \
#    -XX:MaxGCPauseMillis=100 \
#    -XX:+UseStringDeduplication \
#    -Djava.security.egd=file:/dev/./urandom"
#
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

