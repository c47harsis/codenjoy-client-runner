package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.GitService;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenjoy.clientrunner.service.facade.LogWriter.APP_LOG;
import static com.codenjoy.clientrunner.service.facade.LogWriter.BUILD_LOG;

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

    private File getSolutionDirectory(Token token) {
        return new File(String.format("%s/%s/%s/%s",
                config.getSolutionFolder().getPath(),
                token.getPlayerId(), token.getCode(),
                now()));
    }

    private String now() {
        return LocalDateTime.now().format(config.getSolutionFolderFormatter());
    }

    private Token parse(String serverUrl) {
        return new Token(serverUrl, config.getServerRegex());
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
        return new SolutionSummary(solutionManager.getSolution(token, solutionId));
    }

    public List<String> getBuildLogs(String serverUrl, int solutionId, int offset) {
        Token token = parse(serverUrl);
        Solution solution = solutionManager.getSolution(token, solutionId);
        return readFile(solution.getSources() + BUILD_LOG, offset);
    }

    public List<String> getRuntimeLogs(String serverUrl, int solutionId, int offset) {
        Token token = parse(serverUrl);
        Solution solution = solutionManager.getSolution(token, solutionId);
        return readFile(solution.getSources() + APP_LOG, offset);
    }

    private List<String> readFile(String filePath, int offset) {
        try (Stream<String> log = Files.lines(Paths.get(filePath))) {
            return log.skip(offset).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
