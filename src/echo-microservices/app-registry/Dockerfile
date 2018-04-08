FROM openjdk:8-jdk

MAINTAINER Maximilian Irro

#ENV MONGO_URL mongodb://localhost/belfastjug_sample_01

ADD build/libs/registry-1.0-SNAPSHOT.jar /app.jar

RUN sh -c 'touch /app.jar'
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

EXPOSE 3036
