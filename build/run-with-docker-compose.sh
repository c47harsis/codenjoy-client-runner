cd ..
COMPOSE_DOCKER_CLI_BUILD=1 \
  DOCKER_BUILDKIT=1 \
  docker-compose \
  -f ./docker-compose/docker-compose.yml \
  --env-file ./docker-compose/.env \
  -p client_runner \
  up --build