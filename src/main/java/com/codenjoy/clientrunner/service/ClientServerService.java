package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummaryDto;
import lombok.AllArgsConstructor;
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
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(checkRequest.getCodenjoyUrl());
        String playerId = playerIdAndCode.first;
        String code = playerIdAndCode.second;

        File directory = new File(String.format("./%s/%s/%s/%s", config.getSolutionFolderPath(), playerId, code,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(config.getSolutionFolderPattern()))));

        // TODO: async
        Git repo = gitService.clone(checkRequest.getRepoUrl(), directory);

        if (repo == null) {
            throw new IllegalArgumentException("Can not clone repository: " + checkRequest.getRepoUrl());
        }

        dockerRunnerService.runSolution(directory, playerId, code, checkRequest.getCodenjoyUrl());
    }

    private Pair<String, String> extractPlayerIdAndCode(String url) {
        Pattern serverUrlPattern = Pattern.compile(config.getCodenjoyUrlRegex());
        Matcher matcher = serverUrlPattern.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Given invalid server URL: '%s' is not match '%s'",
                            url, config.getCodenjoyUrlRegex()));
        }
        return new Pair<>(matcher.group(1), matcher.group(2));
    }

    public void killSolution(String codenjoyUrl, Integer solutionId) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(codenjoyUrl);
        dockerRunnerService.kill(playerIdAndCode.first, playerIdAndCode.second, solutionId);
    }

    public List<SolutionSummaryDto> getAllSolutionsSummary(String codenjoyUrl) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(codenjoyUrl);
        return dockerRunnerService.getAllSolutionsSummary(playerIdAndCode.first, playerIdAndCode.second);
    }

    public SolutionSummaryDto getSolutionSummary(String codenjoyUrl, Integer solutionId) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(codenjoyUrl);
        return dockerRunnerService.getSolutionSummary(solutionId, playerIdAndCode.first, playerIdAndCode.second);
    }

    public List<String> getBuildLogs(String codenjoyUrl, Integer solutionId, Integer startFromLine) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(codenjoyUrl);
        return dockerRunnerService.getBuildLogs(solutionId, playerIdAndCode.first, playerIdAndCode.second, startFromLine);
    }

    public List<String> getRuntimeLogs(String codenjoyUrl, Integer solutionId, Integer startFromLine) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(codenjoyUrl);
        return dockerRunnerService.getRuntimeLogs(solutionId, playerIdAndCode.first, playerIdAndCode.second, startFromLine);
    }

    @AllArgsConstructor
    private final static class Pair<F, S> {
        private final F first;
        private final S second;
    }
}
