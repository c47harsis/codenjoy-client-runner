package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ClientServerService {

    private final ClientServerServiceConfig config;

    private final GitService gitService;
    private final DockerRunnerService dockerRunnerService;

    public void checkSolution(CheckRequest checkRequest) {
        Server playerIdAndCode = extractPlayerIdAndCode(checkRequest.getServer());
        String playerId = playerIdAndCode.playerId;
        String code = playerIdAndCode.code;

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
        Pattern serverUrlPattern = Pattern.compile(config.getServerRegex());
        Matcher matcher = serverUrlPattern.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Given invalid server URL: '%s' is not match '%s'",
                            url, config.getServerRegex()));
        }
        return new Server(matcher.group(1), matcher.group(2));
    }

    public void killSolution(String server, int solutionId) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        dockerRunnerService.kill(playerIdAndCode.playerId, playerIdAndCode.code, solutionId);
    }

    public List<SolutionSummary> getAllSolutionsSummary(String server) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getAllSolutionsSummary(playerIdAndCode.playerId, playerIdAndCode.code);
    }

    public SolutionSummary getSolutionSummary(String server, int solutionId) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getSolutionSummary(solutionId, playerIdAndCode.playerId, playerIdAndCode.code);
    }

    public List<String> getBuildLogs(String server, int solutionId, int offset) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getBuildLogs(solutionId, playerIdAndCode.playerId, playerIdAndCode.code, offset);
    }

    public List<String> getRuntimeLogs(String server, int solutionId, int offset) {
        Server playerIdAndCode = extractPlayerIdAndCode(server);
        return dockerRunnerService.getRuntimeLogs(solutionId, playerIdAndCode.playerId, playerIdAndCode.code, offset);
    }

    @Data
    private final static class Server {
        private final String playerId;
        private final String code;
    }
}
