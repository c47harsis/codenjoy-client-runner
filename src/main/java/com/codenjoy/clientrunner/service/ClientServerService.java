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
        File directory = getSolutionDirectory(server);
        Git repo = git.clone(request.getRepo(), directory); // TODO: async

        if (repo == null) {
            throw new IllegalArgumentException("Can not clone repository: " +
                    request.getRepo());
        }

        docker.runSolution(directory,
                server.getPlayerId(), server.getCode(),
                request.getServer());
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
        return docker.getSolutionSummary(solutionId, parse(server));
    }

    public List<String> getBuildLogs(String server, int solutionId, int offset) {
        return docker.getBuildLogs(solutionId, parse(server), offset);
    }

    public List<String> getRuntimeLogs(String server, int solutionId, int offset) {
        return docker.getRuntimeLogs(solutionId, parse(server), offset);
    }

}
