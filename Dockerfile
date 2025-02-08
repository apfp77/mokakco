# Base image 선택 (Java 21 지원 이미지)
FROM eclipse-temurin:21-jdk-alpine

# 작업 디렉터리 생성
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar
COPY .env .env

RUN mkdir -p /app/logs

# 앱 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]