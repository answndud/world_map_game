# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --create-home --uid 10001 spring

ENV JAVA_RUNTIME_OPTS="-XX:MaxRAMPercentage=75.0"

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

STOPSIGNAL SIGTERM

ENTRYPOINT ["sh", "-c", "exec java $JAVA_RUNTIME_OPTS -jar /app/app.jar"]
