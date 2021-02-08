FROM ruby:3.0.0-alpine

ARG CODENJOY_URL
ENV CODENJOY_URL_VAR=$CODENJOY_URL

WORKDIR /app

ADD Gemfile /app/
ADD game.rb /app/
RUN bundle install

ENTRYPOINT ruby game.rb "${CODENJOY_URL_VAR}"