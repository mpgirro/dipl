FROM openjdk:8-jdk-alpine

MAINTAINER Maximilian Irro

VOLUME /tmp
ADD build/libs/parser-1.0-SNAPSHOT-all.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]


EXPOSE 3034
