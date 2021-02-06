FROM ruby:3.0.0-alpine

WORKDIR /app

ADD Gemfile /app/
ADD game.rb /app/
RUN bundle install

ENTRYPOINT ruby game.rb