FROM node:12

ARG CODENJOY_URL
ENV CODENJOY_URL_VAR=$CODENJOY_URL

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install 2>&1
COPY . .

ENTRYPOINT node runner.js "${CODENJOY_URL_VAR}"