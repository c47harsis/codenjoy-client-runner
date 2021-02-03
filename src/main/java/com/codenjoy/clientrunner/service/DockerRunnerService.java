package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.DockerConfig;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Server;
import com.codenjoy.clientrunner.model.Solution;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
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

import static com.codenjoy.clientrunner.model.Status.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerRunnerService {

    public static final String SERVER_PARAMETER = "CODENJOY_URL";
    private final DockerConfig config;
    private HostConfig hostConfig;
    private DockerService docker;
    private final Set<Solution> solutions = ConcurrentHashMap.newKeySet();

    private void killLastIfPresent(Server server) {
        getSolutions(server).stream()
                .filter(s -> s.getStatus().isActive())
                .forEach(this::kill);
    }

    @PostConstruct
    protected void init() {
        hostConfig = HostConfig.newHostConfig()
                .withCpuPeriod(config.getContainer().getCpuPeriod())
                .withCpuQuota(config.getContainer().getCpuQuota())
                .withMemory(config.getContainer().getMemoryLimitBytes());
    }

    // TODO: Refactor this
    @SneakyThrows
    public void runSolution(Server server, File sources) {
        Solution solution = new Solution(server, sources);
        addDockerfile(solution);
        killLastIfPresent(server);

        /* TODO: try to avoid copy Dockerfile. https://docs.docker.com/engine/api/v1.41/#operation/ImageBuild */
        solutions.add(solution);

        if (!solution.getStatus().isActive()) {
            log.debug("Attempt to run inactive solution with id: {} and status: {}", solution.getId(), solution.getStatus());
            return;
        }

        try {
            solution.setStatus(COMPILING);
            LogWriter writer = new LogWriter(solution, true);
            docker.buildImageCmd(solution.getSources())
                    .withBuildArg(SERVER_PARAMETER, solution.getServer())
                    .exec(new BuildImageResultCallback() {
                        private String imageId;
                        private String error;

                        @Override
                        public void onNext(BuildResponseItem item) {
                            if (item.getStream() != null) {
                                writer.write(item.getStream());
                            }
                            if (item.isBuildSuccessIndicated()) {
                                this.imageId = item.getImageId();
                            } else if (item.isErrorIndicated()) {
                                this.error = item.getError();
                            }
                        }

                        @SneakyThrows
                        @Override
                        public void onComplete() {
                            writer.close();
                            if (solution.getStatus() == KILLED) {
                                super.onComplete();
                                return;
                            }
                            solution.setImageId(imageId);
                            solution.setStatus(RUNNING);
                            solution.setStarted(LocalDateTime.now());
                            String containerId = docker.createContainer(imageId, hostConfig);
                            solution.setContainerId(containerId);

                            docker.startContainer(solution);

                            LogWriter writer = new LogWriter(solution, false);
                            docker.logContainer(solution, writer);

                            docker.waitContainer(solution, () -> {
                                solution.setFinished(LocalDateTime.now());
                                if (solution.getStatus() == KILLED) {
                                    solution.setStatus(FINISHED);
                                }
                                docker.removeContainer(solution);
                                // TODO: remove images
                            });
                            super.onComplete();
                        }
                    });
        } catch (Throwable e) {
            if (!KILLED.equals(solution.getStatus())) {
                solution.setStatus(ERROR);
            }
        }
    }

    public void kill(Server server, int solutionId) {
        kill(getSolution(server, solutionId));
    }

    private void kill(Solution solution) {
        if (!solution.getStatus().isActive()) {
            return;
        }
        solution.setStatus(KILLED);
        if (solution.getContainerId() != null) {
            docker.killContainer(solution);
        }
    }

    private void addDockerfile(Solution solution) {
        try {
            File destination = new File(solution.getSources(), "Dockerfile");

            String relative = "dockerfiles/java/Dockerfile";
            File inSource = new File("./" + relative);
            if (!inSource.exists()) {
                inSource = new File("./client-runner/" + relative);
            }
            if (inSource.exists()) {
                FileUtils.copyFile(inSource, destination);
                log.debug("java-dockerfile copied from sources");
                return;
            }

            URL inJar = getClass().getResource("/WEB-INF/classes/" + relative);
            FileUtils.copyURLToFile(inJar, destination);
            log.debug("java-dockerfile copied from jar");
        } catch (IOException e) {
            log.error("Can not add Dockerfile to solution with id: {}", solution.getId());
            solution.setStatus(ERROR);
        }
    }

    public List<Solution> getSolutions(Server server) {
        return solutions.stream()
                .filter(server::applicable)
                .collect(toList());
    }

    public Solution getSolution(Server server, int solutionId) {
        return getSolutions(server).stream()
                .filter(s -> solutionId == s.getId())
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public List<SolutionSummary> getAllSolutionsSummary(Server server) {
        return getSolutions(server).stream()
                .map(SolutionSummary::new)
                .sorted(Comparator.comparingInt(SolutionSummary::getId))
                .collect(toList());
    }
}