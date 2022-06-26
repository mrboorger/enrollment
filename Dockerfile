# syntax=docker/dockerfile:1

FROM openjdk:17-jdk-slim-buster AS builder

# RUN apt-get update -y
# RUN apt-get install -y binutils

WORKDIR /app

EXPOSE 80

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw", "spring-boot:run"]
