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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class ClientServerService {

    private final ClientServerServiceConfig config;

    private final GitService gitService;
    private final DockerRunnerService dockerRunnerService;

    public void checkSolution(CheckRequest checkRequest) {
        Server playerIdAndCode = extractPlayerIdAndCode(checkRequest.getServer());
        String playerId = playerIdAndCode.getPlayerId();
        String code = playerIdAndCode.getCode();

        File directory = new File(String.format("./%s/%s/%s/%s", config.getSolutionFolder().getPath(), playerId, code,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(config.getSolutionFolder().getPattern()))));

        // TODO: async
        Git repo = gitService.clone(checkRequest.getRepo(), directory);

        if (repo == null) {
            throw new IllegalArgumentException("Can not clone repository: " + checkRequest.getRepo());
        }

        dockerRunnerService.runSolution(directory, playerId, code, checkRequest.getServer());
    }

    private Server extractPlayerIdAndCode(String url) {
        return new Server(url, config.getServerRegex());
    }

    public void killSolution(String server, int solutionId) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        dockerRunnerService.kill(playerIdAndCode.getPlayerId(), playerIdAndCode.getCode(), solutionId);
    }

    public List<SolutionSummary> getAllSolutionsSummary(String server) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getAllSolutionsSummary(playerIdAndCode.getPlayerId(), playerIdAndCode.getCode());
    }

    public SolutionSummary getSolutionSummary(String server, int solutionId) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getSolutionSummary(solutionId, playerIdAndCode.getPlayerId(), playerIdAndCode.getCode());
    }

    public List<String> getBuildLogs(String server, int solutionId, int offset) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getBuildLogs(solutionId, playerIdAndCode.getPlayerId(), playerIdAndCode.getCode(), offset);
    }

    public List<String> getRuntimeLogs(String server, int solutionId, int offset) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getRuntimeLogs(solutionId, playerIdAndCode.getPlayerId(), playerIdAndCode.getCode(), offset);
    }

}
