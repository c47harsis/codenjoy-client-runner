package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.DockerConfig;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.exception.SolutionNotFoundException;
import com.codenjoy.clientrunner.component.IdGenerator;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.DockerService;
import com.codenjoy.clientrunner.service.facade.LogWriter;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenjoy.clientrunner.model.Solution.Status.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionManager {

    private final IdGenerator idGenerator;
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

    public void runSolution(Token token, File sources) {
        getSolutions(token).forEach(this::kill);

        Solution solution = Solution.from(token, sources);
        solution.setId(idGenerator.next());
        solutions.add(solution);

        /* TODO: try to avoid copy Dockerfile. https://docs.docker.com/engine/api/v1.41/#operation/ImageBuild */
        addDockerfile(solution);

        if (!solution.getStatus().isActive()) {
            log.debug("Attempt to run inactive solution with id: {} and status: {}",
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

    public void killAll(Token token) {
        getSolutions(token).forEach(this::kill);
    }

    public void kill(Token token, int solutionId) {
        Solution solution = getSolution(token, solutionId)
                .orElseThrow(() -> new SolutionNotFoundException(solutionId));
        kill(solution);
    }

    public SolutionSummary getSolutionSummary(Token token, int solutionId) {
        return getSolution(token, solutionId)
                .map(SolutionSummary::new)
                .orElseThrow(() -> new SolutionNotFoundException(solutionId));
    }

    public List<SolutionSummary> getAllSolutionSummary(Token token) {
        return getSolutions(token).stream()
                .map(SolutionSummary::new)
                .sorted(Comparator.comparingInt(SolutionSummary::getId))
                .collect(toList());
    }

    public List<String> getLogs(Token token, int solutionId, LogType logType, int offset) {
        Solution solution = getSolution(token, solutionId)
                .orElseThrow(() -> new SolutionNotFoundException(solutionId));

        if (!logType.existsWhen(solution.getStatus())) {
            return Collections.emptyList();
        }

        return readLogs(solution, logType, offset);
    }

    private List<Solution> getSolutions(Token token) {
        return solutions.stream()
                .filter(s -> s.allows(token))
                .collect(toList());
    }

    private Optional<Solution> getSolution(Token token, int solutionId) {
        return getSolutions(token).stream()
                .filter(s -> s.getId() == solutionId)
                .findFirst();
    }

    private void runContainer(Solution solution, String imageId) {
        if (solution.getStatus() == KILLED) {
            log.info("Attempt to run killed solution with id: {}", solution.getId());
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

    private void addDockerfile(Solution solution) {
        String platformFolder = solution.getPlatform().getFolderName();
        try {
            File destination = new File(solution.getSources(), "Dockerfile");
            String path = config.getDockerfilesFolder() + "/" + platformFolder + "/Dockerfile";
            URL url = getClass().getResource(path);
            FileUtils.copyURLToFile(url, destination);
        } catch (IOException e) {
            log.error("Can not add Dockerfile to solution with id: {}", solution.getId());
            solution.setStatus(ERROR);
        }
    }

    private List<String> readLogs(Solution solution, LogType type, int offset) {
        String logFilePath = solution.getSources() + "/" + type.getFilename();
        try (Stream<String> log = Files.lines(Paths.get(logFilePath))) {
            return log.skip(offset).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Log file not exists: " + logFilePath);
            throw new IllegalStateException("Solution with id: " + solution.getId()
                    + " is in " + solution.getStatus() + " status, but build log not exists");
        }
    }

}