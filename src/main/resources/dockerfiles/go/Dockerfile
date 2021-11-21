FROM golang:1.16-alpine

ARG SERVER_URL
ENV SERVER_URL_VAR=$SERVER_URL

ARG GAME_TO_RUN
ENV GAME_TO_RUN_VAR=$GAME_TO_RUN

RUN apk fix
RUN apk --update add git less openssh && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*

WORKDIR /app

COPY . ./

ENTRYPOINT go run ./main.go "${GAME_TO_RUN_VAR}" "${SERVER_URL_VAR}"