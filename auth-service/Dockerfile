# ===== 1단계: 빌드 =====
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY auth-service ./auth-service

RUN ./gradlew :auth-service:bootJar --no-daemon -x test

# ===== 2단계: 실행 =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/auth-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
