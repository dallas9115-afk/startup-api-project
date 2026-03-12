# 1. AWS에 최적화된 Amazon Corretto 17 버전의 가벼운(alpine) 리눅스 환경을 가져옵니다.
FROM amazoncorretto:17-alpine

# 2. 컨테이너 내부에 작업 디렉토리를 생성합니다.
WORKDIR /app

# 3. 빌드된 jar 파일을 컨테이너 내부로 복사합니다.
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 4. 컨테이너가 켜질 때 실행할 명령어를 지정합니다. (운영 모드로 실행)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]