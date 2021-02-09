# CODENJOY Solution Runner
Makes possible to store [Codenjoy][0] players solutions and check them in isolated manner. 

## Introduction
For many and many years [Codenjoy][0] players had to configure and run their game clients locally.
The clients should connect to a game server via websockets and try to pass a level using player's 
custom implementation of an algorithm. In such circumstances there are some problems, where one of them is
inability to save the history of attempts in one place, and another, which is more important, even if 
it was possible, we could not be sure if the attempts are successful of not until we launch them by ourselves.

So this application is intended to solve these problems described above.

[0]: (https://github.com/codenjoyme/codenjoy)
