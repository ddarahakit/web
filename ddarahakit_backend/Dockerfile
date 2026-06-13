# ===== 빌드 스테이지 =====
FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace

# 의존성 캐시 최적화: 빌드 스크립트 먼저 복사
COPY settings.gradle build.gradle ./

# 모놀리식 소스 복사 후 부트 JAR 빌드
COPY src ./src
RUN gradle :bootJar --no-daemon -x test

# ===== 런타임 스테이지 =====
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 비루트 사용자 (uid/gid 1000 → 매니페스트의 fsGroup 과 일치)
RUN groupadd -g 1000 app && useradd -u 1000 -g app -m app \
    && mkdir -p /app/uploads && chown -R app:app /app

# bootJar 산출물만 복사 (-plain.jar 은 매칭되지 않음)
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar /app/app.jar

USER app
EXPOSE 8080

# 컨테이너 메모리 한도에 맞춰 힙 자동 조정
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
