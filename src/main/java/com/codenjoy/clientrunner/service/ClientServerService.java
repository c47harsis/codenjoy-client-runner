package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenjoy.clientrunner.model.Solution.Status.*;
import static com.codenjoy.clientrunner.service.facade.LogWriter.*;

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
        return solutionManager.getAllSolutionsSummary(token);
    }

    public SolutionSummary getSolutionSummary(String serverUrl, int solutionId) {
        Token token = parse(serverUrl);
        Solution solution = solutionManager.getSolution(token, solutionId);
        if (solution == null) {
            throw new IllegalArgumentException("Solution with id: " + solutionId + " not found");
        }
        return new SolutionSummary(solution);
    }

    public List<String> getBuildLogs(String serverUrl, int solutionId, int offset) {
        return getLogs(serverUrl, solutionId, offset, BUILD_LOG, NEW);
    }

    public List<String> getRuntimeLogs(String serverUrl, int solutionId, int offset) {
        return getLogs(serverUrl, solutionId, offset, RUNTIME_LOG, NEW, COMPILING);
    }

    private List<String> getLogs(String serverUrl, int solutionId, int offset,
                                 String logFile, Solution.Status... excluded)
    {
        Token token = parse(serverUrl);
        Solution solution = solutionManager.getSolution(token, solutionId);
        if (solution == null) {
            throw new IllegalArgumentException("Solution with id: " + solutionId + " not found");
        }
        if (Arrays.asList(excluded).contains(solution.getStatus())) {
            return Collections.emptyList();
        }
        return readFile(solution.getSources() + logFile, offset);
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

    private List<String> readFile(String filePath, int offset) {
        try (Stream<String> log = Files.lines(Paths.get(filePath))) {
            return log.skip(offset).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Can not read log : " + filePath);
            return Collections.emptyList();
        }
    }

}
