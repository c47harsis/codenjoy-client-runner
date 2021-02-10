package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ClientServerService {

    private final ClientServerServiceConfig config;
    private final GitService git;
    private final SolutionManager solutionManager;

    public void checkSolution(CheckRequest request) {
        Token token = parse(request.getServerUrl());
        File directory = getSolutionDirectory(token);

        Git repo = git.clone(request.getRepo(), directory); // TODO: async
        if (repo == null) {
            throw new IllegalArgumentException("Can not clone repository: " +
                    request.getRepo());
        }

        solutionManager.runSolution(token, directory);
    }

    public void killSolution(String serverUrl, int solutionId) {
        Token token = parse(serverUrl);
        solutionManager.kill(token, solutionId);
    }

    public List<SolutionSummary> getAllSolutionsSummary(String serverUrl) {
        Token token = parse(serverUrl);
        return solutionManager.getAllSolutionSummary(token);
    }

    public SolutionSummary getSolutionSummary(String serverUrl, int solutionId) {
        Token token = parse(serverUrl);
        return solutionManager.getSolutionSummary(token, solutionId);
    }

    public List<String> getLogs(String serverUrl, int solutionId, LogType logType, int offset) {
        Token token = parse(serverUrl);
        return solutionManager.getLogs(token, solutionId, logType, offset);
    }

    private Token parse(String serverUrl) {
        return Token.from(serverUrl, config.getServerRegex());
    }

    private File getSolutionDirectory(Token token) {
        return new File(String.format("%s/%s/%s/%s",
                config.getSolutionFolder().getPath(),
                token.getPlayerId(), token.getCode(),
                now()));
    }

    private String now() {
        String pattern = config.getSolutionFolder().getPattern();
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(pattern));
    }
}