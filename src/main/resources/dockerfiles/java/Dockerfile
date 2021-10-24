# syntax=docker/dockerfile:experimental

# build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /app/src
COPY pom.xml /app
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/pom.xml -B clean compile assembly:single -DskipTests=true -Pjar-with-dependencies,noGit

# package stage
FROM openjdk:11-jre-slim

ARG SERVER_URL
ENV SERVER_URL_VAR=$SERVER_URL

ARG GAME_TO_RUN
ENV GAME_TO_RUN_VAR=$GAME_TO_RUN

COPY --from=build /app/target/*-exec.jar /app.jar
ENTRYPOINT java -jar /app.jar "${GAME_TO_RUN_VAR}" "${SERVER_URL_VAR}"