# CODENJOY Solution Runner
Makes possible to store [Codenjoy][0] players solutions and check them in isolated manner. 

## Introduction
For many and many years [Codenjoy][0] players had to configure and run their game clients locally.
The clients should connect to a game server via websockets and try to pass a level using player's 
custom implementation of an algorithm. In such circumstances there are some problems.
One of them is an inability to save the history of attempts in one place, and another one,
which is more important, even if it could be possible, 
we can not be sure if the attempts are successful of not until we launch them by ourselves.

So this application is intended to solve these problems described above and 
brings [Codenjoy][0] and it's users several new opportunities. For example, 
it could be also a _testing and hiring system_ for modern and progressive
companies and teams.

## How it works
The app uses [Git](https://git-scm.com/) and [Docker](https://www.docker.com/) for its purposes. 
It consumes requests that contains URL of public Git repository with a player's solution
and link to Codenjoy server with the player's id and code. Further, it pulls the solution 
from master branch of the repo, save it locally with, build it and run it in new separate 
Docker container. Build and runtime logs accumulate in the solution folder and can be given 
to front-end.

## How to run
### Run with Maven right on the local host
The command below will build the application using Maven and run in right on the local host:
```
$ sh ./scripts/run_with_maven.sh
```
For configuring application's behaviour you can set these environment variables.

Variable | Defaults | Description
---------|:----------:|------------
`SERVICE_CONTEXT` | /client-runner | Web app context
`SOLUTIONS_FOLDER_PATH` | ./solutions | Where to store downloaded solutions
`SOLUTION_FOLDER_PATTERN` | yyyy-MM-dd'_'HH-mm-ss | How to name each solution folder
`DOCKER_MEMORY_LIMIT_MB` | 0 | [Memory limit in MB](https://docs.docker.com/engine/reference/commandline/build/)
`DOCKER_CPU_PERIOD` | 100000 | [Limit the CPU CFS (Completely Fair Scheduler) period](https://docs.docker.com/engine/reference/commandline/build/)
`DOCKER_CPU_QUOTA` | -1 | [Limit the CPU CFS quota](https://docs.docker.com/engine/reference/commandline/build/)

### Run in Docker container  [Recommended]
The command below will build and start a container with the app and with
default config:
```
$ sh ./scripts/run_with_docker-compose.sh
```
For configuring container's port or folder path, where solutions will be stored,
change `docker-compose/.env` file:
```
$ nano ./docker-compose/.env
```
For configuring application's behaviour,
change `docker-compose/client-runner.env` file with variables from the table above:
```
& nano ./docker-compose/client-runner.env
```

[0]: (https://github.com/codenjoyme/codenjoy)
