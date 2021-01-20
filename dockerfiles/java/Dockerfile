# syntax=docker/dockerfile:experimental

# build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /app/src
COPY pom.xml /app
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/pom.xml package -Dmaven.test.skip=true \
    && cp /app/target/*-exec.jar /app.jar

# package stage
FROM openjdk:11-jre-slim
ARG CODENJOY_URL
ENV CODENJOY_URL_VAR=$CODENJOY_URL
COPY --from=build /app.jar /usr/local/lib/app.jar
ENTRYPOINT java -jar /usr/local/lib/app.jar ${CODENJOY_URL_VAR}