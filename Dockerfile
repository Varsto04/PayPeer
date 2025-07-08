FROM openjdk:21-jdk-slim
WORKDIR /app

COPY pom.xml pom.xml

RUN apt-get update \
 && apt-get install -y maven netcat-openbsd \
 && mvn dependency:go-offline -B

COPY src ./src
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]