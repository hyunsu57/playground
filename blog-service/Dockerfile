# ===== 1단계: 빌드 =====
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY blog-service ./blog-service

RUN ./gradlew :blog-service:bootJar --no-daemon -x test

# ===== 2단계: 실행 =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/blog-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
