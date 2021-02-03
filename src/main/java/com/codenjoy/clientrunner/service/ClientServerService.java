package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Server;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ClientServerService {

    private final ClientServerServiceConfig config;
    private final GitService git;
    private final DockerRunnerService docker;

    public void checkSolution(CheckRequest request) {
        Server server = parse(request.getServer());

        File directory = new File(String.format("./%s/%s/%s/%s",
                config.getSolutionFolder().getPath(),
                server.getPlayerId(), server.getCode(),
                now()));

        // TODO: async
        Git repo = git.clone(request.getRepo(), directory);

        if (repo == null) {
            throw new IllegalArgumentException("Can not clone repository: " +
                    request.getRepo());
        }

        docker.runSolution(directory,
                server.getPlayerId(), server.getCode(),
                request.getServer());
    }

    private String now() {
        return LocalDateTime.now().format(config.getSolutionFolderFormatter());
    }

    private Server parse(String url) {
        return new Server(url, config.getServerRegex());
    }

    public void killSolution(String server, int solutionId) {
        Server playerIdAndCode = parse(server);
        docker.kill(playerIdAndCode.getPlayerId(), playerIdAndCode.getCode(), solutionId);
    }

    public List<SolutionSummary> getAllSolutionsSummary(String server) {
        Server playerIdAndCode = parse(server);
        return docker.getAllSolutionsSummary(playerIdAndCode.getPlayerId(), playerIdAndCode.getCode());
    }

    public SolutionSummary getSolutionSummary(String server, int solutionId) {
        Server playerIdAndCode = parse(server);
        return docker.getSolutionSummary(solutionId, playerIdAndCode.getPlayerId(), playerIdAndCode.getCode());
    }

    public List<String> getBuildLogs(String server, int solutionId, int offset) {
        Server playerIdAndCode = parse(server);
        return docker.getBuildLogs(solutionId, playerIdAndCode.getPlayerId(), playerIdAndCode.getCode(), offset);
    }

    public List<String> getRuntimeLogs(String server, int solutionId, int offset) {
        Server playerIdAndCode = parse(server);
        return docker.getRuntimeLogs(solutionId, playerIdAndCode.getPlayerId(), playerIdAndCode.getCode(), offset);
    }

}
