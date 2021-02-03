package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Server;
import com.codenjoy.clientrunner.model.Solution;
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

@Service
@AllArgsConstructor
public class ClientServerService {

    public static final String BUILD_LOG = "/build.log";
    public static final String APP_LOG = "/app.log";

    private final ClientServerServiceConfig config;
    private final GitService git;
    private final DockerRunnerService docker;

    public void checkSolution(CheckRequest request) {
        Server server = parse(request.getServer());
        File directory = getSolutionDirectory(server);
        Git repo = git.clone(request.getRepo(), directory); // TODO: async

        if (repo == null) {
            throw new IllegalArgumentException("Can not clone repository: " +
                    request.getRepo());
        }

        docker.runSolution(directory, server);
    }

    private File getSolutionDirectory(Server server) {
        return new File(String.format("./%s/%s/%s/%s",
                config.getSolutionFolder().getPath(),
                server.getPlayerId(), server.getCode(),
                now()));
    }

    private String now() {
        return LocalDateTime.now().format(config.getSolutionFolderFormatter());
    }

    private Server parse(String url) {
        return new Server(url, config.getServerRegex());
    }

    public void killSolution(String server, int solutionId) {
        docker.kill(parse(server), solutionId);
    }

    public List<SolutionSummary> getAllSolutionsSummary(String server) {
        return docker.getAllSolutionsSummary(parse(server));
    }

    public SolutionSummary getSolutionSummary(String server, int solutionId) {
        return new SolutionSummary(docker.getSolution(parse(server), solutionId));
    }

    public List<String> getBuildLogs(String server, int solutionId, int offset) {
        Solution solution = docker.getSolution(parse(server), solutionId);
        return readFile(solution.getSources() + BUILD_LOG, offset);
    }

    public List<String> getRuntimeLogs(String server, int solutionId, int offset) {
        Solution solution = docker.getSolution(parse(server), solutionId);
        return readFile(solution.getSources() + APP_LOG, offset);
    }

    private List<String> readFile(String filePath, int offset) {
        try (Stream<String> log = Files.lines(Paths.get(filePath))) {
            return log.skip(offset).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

}
