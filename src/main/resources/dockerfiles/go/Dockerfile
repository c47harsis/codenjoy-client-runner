FROM golang:1.12.0-alpine3.9

ARG SERVER_URL
ENV SERVER_URL_VAR=$SERVER_URL

ARG GAME_TO_RUN
ENV GAME_TO_RUN_VAR=$GAME_TO_RUN

RUN apk fix
RUN apk --update add git less openssh && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*

COPY . /app

WORKDIR /app

ENTRYPOINT go run main.go "${GAME_TO_RUN_VAR}" "${SERVER_URL_VAR}"