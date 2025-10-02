# Dockerfile
FROM eclipse-temurin:21-jdk-alpine

# 타임존(옵션)
ENV TZ=Asia/Seoul

# JAR 경로 인자화 (기본: build/libs/*SNAPSHOT.jar)
ARG JAR_FILE=build/libs/*SNAPSHOT.jar

# 실행 JAR 복사
COPY ${JAR_FILE} /app/project.jar

WORKDIR /app

# 헬스체크(옵션: 관리 편의)
HEALTHCHECK --interval=30s --timeout=3s --retries=5 CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","project.jar","--spring.profiles.active=prod"]