FROM alpine:3.14.2

ARG SERVER_URL
ENV SERVER_URL_VAR=$SERVER_URL

ARG GAME_TO_RUN
ENV GAME_TO_RUN_VAR=$GAME_TO_RUN

WORKDIR /app

COPY . .

# some build stuff
# RUN apk add --update util-linux

# this will help the container stay running
# ENTRYPOINT tail -f /dev/null

ENTRYPOINT lang ./main.lng "${GAME_TO_RUN_VAR}" "${SERVER_URL_VAR}"
