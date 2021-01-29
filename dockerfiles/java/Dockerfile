# syntax=docker/dockerfile:experimental

# build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /app/src
COPY .git /app/.git
COPY pom.xml /app
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/pom.xml -B clean compile assembly:single -DskipTests=true -DgitDir=/app -Pjar-with-dependencies

# package stage
FROM openjdk:11-jre-slim
ARG CODENJOY_URL
ENV CODENJOY_URL_VAR=$CODENJOY_URL
COPY --from=build /app/target/*-exec.jar /app.jar
ENTRYPOINT java -jar /app.jar "${CODENJOY_URL_VAR}"