# ============================================================
# Momen - Multi-stage Dockerfile (Spring Boot, Java 21)
# ============================================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle wrapper + 설정 파일만 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (소스 변경 없이 이 레이어만 재사용)
RUN ./gradlew dependencies --no-daemon || true

COPY src src

# JAR 빌드 (테스트는 Jenkins에서 실행, 이미지 빌드 시에는 제외)
RUN ./gradlew bootJar --no-daemon -x test \
    && mv build/libs/*.jar app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 보안: non-root 사용자
RUN addgroup -g 1000 app && adduser -u 1000 -G app -D app
USER app

COPY --from=builder /app/app.jar app.jar

# Spring Boot actuator / 헬스체크
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
