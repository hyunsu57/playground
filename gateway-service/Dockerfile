# ===== 1단계: 빌드 (JDK 21 포함 이미지) =====
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle Wrapper와 빌드 스크립트만 먼저 복사 (의존성 캐시 활용)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY gateway-service ./gateway-service

# gateway-service만 빌드 (테스트 제외, 속도 향상)
RUN ./gradlew :gateway-service:bootJar --no-daemon -x test

# ===== 2단계: 실행 (JRE만 포함한 경량 이미지) =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 결과물(실행 가능한 JAR)만 복사
COPY --from=builder /app/gateway-service/build/libs/*.jar app.jar

EXPOSE 8080

# 컨테이너 시작 시 Spring Boot 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
