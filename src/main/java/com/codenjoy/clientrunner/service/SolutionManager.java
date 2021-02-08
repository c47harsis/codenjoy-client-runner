package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.DockerConfig;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.DockerService;
import com.codenjoy.clientrunner.service.facade.LogWriter;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.codenjoy.clientrunner.model.Solution.Status.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionManager {

    private final DockerConfig config;
    private HostConfig hostConfig;
    private final DockerService docker;
    private final Set<Solution> solutions = ConcurrentHashMap.newKeySet();

    @PostConstruct
    protected void init() {
        hostConfig = HostConfig.newHostConfig()
                .withCpuPeriod(config.getContainer().getCpuPeriod())
                .withCpuQuota(config.getContainer().getCpuQuota())
                .withMemory(config.getContainer().getMemoryLimitBytes());
    }

    // TODO: Refactor this
    @SneakyThrows
    public void runSolution(Token token, File sources) {
        Solution solution = Solution.from(token, sources);
        addDockerfile(solution);
        kill(token);

        /* TODO: try to avoid copy Dockerfile.
            https://docs.docker.com/engine/api/v1.41/#operation/ImageBuild */
        solutions.add(solution);

        if (!solution.getStatus().isActive()) {
            log.debug("Attempt to run inactive solution " +
                    "with id: {} and status: {}",
                    solution.getId(), solution.getStatus());
            return;
        }

        try {
            solution.setStatus(COMPILING);
            docker.buildImage(sources, solution.getServerUrl(),
                    new LogWriter(solution, true),
                    imageId -> runContainer(solution, imageId));
        } catch (Throwable e) {
            if (solution.getStatus() != KILLED) {
                solution.setStatus(ERROR);
            }
        }
    }

    public void kill(Token token, int solutionId) {
        kill(getSolution(token, solutionId));
    }

    public List<Solution> getSolutions(Token token) {
        return solutions.stream()
                .filter(s -> s.allows(token))
                .collect(toList());
    }

    public Solution getSolution(Token token, int solutionId) {
        return getSolutions(token).stream()
                .filter(s -> s.getId() == solutionId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("For this token not found any solution with id '%s'",
                                solutionId)));
    }

    public List<SolutionSummary> getAllSolutionsSummary(Token token) {
        return getSolutions(token).stream()
                .map(SolutionSummary::new)
                .sorted(Comparator.comparingInt(SolutionSummary::getId))
                .collect(toList());
    }

    private void runContainer(Solution solution, String imageId) {
        if (solution.getStatus() == KILLED) {
            return;
        }
        solution.setImageId(imageId);
        solution.setStatus(RUNNING);
        solution.setStarted(LocalDateTime.now());

        String containerId = docker.createContainer(imageId, hostConfig);
        solution.setContainerId(containerId);

        docker.startContainer(solution.getContainerId());

        docker.logContainer(solution.getContainerId(), new LogWriter(solution, false));

        docker.waitContainer(solution.getContainerId(), () -> {
            solution.setFinished(LocalDateTime.now());
            if (solution.getStatus() == KILLED) {
                solution.setStatus(FINISHED);
            }
            if (solution.getStatus() == RUNNING) {
                solution.setStatus(ERROR);
            }
            docker.removeContainer(solution.getContainerId());
            // TODO: remove images
        });
    }

    private void kill(Solution solution) {
        if (!solution.getStatus().isActive()) {
            return;
        }
        solution.setStatus(KILLED);
        if (solution.getContainerId() != null) {
            docker.killContainer(solution.getContainerId());
        }
    }

    private void kill(Token token) {
        getSolutions(token)
                .forEach(this::kill);
    }

    private void addDockerfile(Solution solution) {
        String language = solution.getPlatform().getFolderName();
        try {
            File destination = new File(solution.getSources(), "Dockerfile");
            String path = config.getDockerfilesFolder() + "/" + language + "/Dockerfile";
            URL url = getClass().getResource(path);
            FileUtils.copyURLToFile(url, destination);
        } catch (IOException e) {
            log.error("Can not add Dockerfile to solution with id: {}", solution.getId());
            solution.setStatus(ERROR);
        }
    }

    // for testing only
    void clear() {
        solutions.clear();
    }

    void add(Solution solution) {
        solutions.add(solution);
    }
}