# syntax=docker/dockerfile:experimental

# build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /app/src
COPY pom.xml /app
RUN --mount=type=cache,target=/root/.m2 mvn -f /app/pom.xml -B clean package -DskipTests=true

# package stage
FROM openjdk:11-jre-slim
COPY --from=build /app/target/codenjoy-client-runner.war /app/client-runner.war
EXPOSE 8080
ENTRYPOINT java -jar /app/client-runner.war