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
from master branch of the repo, save it locally with, build it and run it in new separate Docker container.
Build and runtime logs accumulate in the solution folder and can be given to front-end.



[0]: (https://github.com/codenjoyme/codenjoy)
