# syntax=docker/dockerfile:experimental

# build stage
FROM maven:3.6.0-jdk-11-slim AS build

WORKDIR /app

COPY src ./src/
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 mvn -f ./pom.xml -B clean compile assembly:single -DskipTests=true -Pjar-with-dependencies,noGit

# package stage
FROM openjdk:11-jre-slim

ARG SERVER_URL
ENV SERVER_URL_VAR=$SERVER_URL

ARG GAME_TO_RUN
ENV GAME_TO_RUN_VAR=$GAME_TO_RUN

WORKDIR /app

COPY --from=build /app/target/client-exec.jar ./app.jar

ENTRYPOINT java -jar ./app.jar "$GAME_TO_RUN_VAR" "$SERVER_URL_VAR"