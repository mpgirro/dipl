FROM openjdk:8-jdk-alpine

MAINTAINER Maximilian Irro

#COPY build/libs/*all.jar /app.jar
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]


#ADD build/libs/*all.jar /app.jar
#RUN sh -c 'touch /app.war'
#VOLUME /tmp
#CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.war"]

VOLUME /tmp
ADD build/libs/app-index-1.0-SNAPSHOT-all.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

EXPOSE 3032
