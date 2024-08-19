FROM gradle:8.8-jdk21 AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY build.gradle settings.gradle gradlew ./

RUN chmod +x gradlew

COPY src/ src/

# Gradle 캐시 디렉토리를 명시적으로 설정
ENV GRADLE_USER_HOME /app/.gradle

# Gradle 빌드 명령어 실행 (테스트 건너뛰기)
RUN ./gradlew build -x test --no-daemon --stacktrace

# 빌드 결과를 확인하기 위해 파일 목록을 출력
RUN ls -l /app/build/libs/

FROM openjdk:21-jdk-slim

WORKDIR /app

# 빌드 단계에서 생성된 실행 가능한 JAR 파일을 복사
COPY --from=builder /app/build/libs/pinokkio-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]