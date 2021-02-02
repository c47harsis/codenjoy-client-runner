package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.DockerConfig;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.dto.SolutionSummaryDto;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerRunnerService {

    private final DockerConfig dockerConfig;

    private final DockerClient docker = DockerClientBuilder.getInstance().build();
    private HostConfig hostConfig;

    private final Set<Solution> solutions = ConcurrentHashMap.newKeySet();

    private void killLastIfPresent(String playerId, String code) {
        solutions.stream()
                .filter(s -> playerId.equals(s.getPlayerId()))
                .filter(s -> code.equals(s.getCode()))
                .filter(Solution::isActive)
                .forEach(this::kill);
    }

    @PostConstruct
    protected void init() {
        hostConfig = HostConfig.newHostConfig()
                .withCpuPeriod(dockerConfig.getContainer().getCpuPeriod())
                .withCpuQuota(dockerConfig.getContainer().getCpuQuota())
                .withMemory(dockerConfig.getContainer().getMemoryLimitBytes());
    }

    // TODO: Refactor this
    @SneakyThrows
    public void runSolution(File sources, String playerId, String code, String codenjoyUrl) {
        Solution solution = new Solution(playerId, code, codenjoyUrl, sources);
        addDockerfile(solution);

        killLastIfPresent(playerId, code);

        /* TODO: try to avoid copy Dockerfile. https://docs.docker.com/engine/api/v1.41/#operation/ImageBuild */
        solutions.add(solution);

        if (!solution.isActive()) {
            log.debug("Attempt to run inactive solution with id: {} and status: {}", solution.getId(), solution.getStatus());
            return;
        }

        try {
            solution.setStatus(Solution.Status.COMPILING);
            docker.buildImageCmd(solution.getSources())
                    .withBuildArg("CODENJOY_URL", solution.getCodenjoyUrl())
                    .exec(new BuildImageResultCallback() {
                        private final Writer writer = new BufferedWriter(new FileWriter(solution.getSources() + "/build.log"));
                        private String imageId;
                        private String error;

                        @Override
                        public void onNext(BuildResponseItem item) {
                            if (item.getStream() != null) {
                                try {
                                    writer.write(item.getStream());
                                    writer.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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
                            try {
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (Solution.Status.KILLED.equals(solution.getStatus())) {
                                super.onComplete();
                                return;
                            }
                            solution.setImageId(imageId);
                            solution.setStatus(Solution.Status.RUNNING);
                            solution.setStarted(LocalDateTime.now());
                            String containerId = docker.createContainerCmd(imageId)
                                    .withHostConfig(hostConfig)
                                    .exec().getId();
                            solution.setContainerId(containerId);

                            docker.startContainerCmd(containerId).exec();

                            docker.logContainerCmd(containerId)
                                    .withStdOut(true)
                                    .withStdErr(true)
                                    .withFollowStream(true)
                                    .withTailAll()
                                    .exec(new ResultCallback.Adapter<Frame>() {
                                        final Writer writer = new FileWriter(solution.getSources() + "/app.log");

                                        @Override
                                        public void onNext(Frame object) {
                                            try {
                                                writer.write(object.toString() + "\n");
                                                writer.flush();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onComplete() {
                                            try {
                                                writer.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            super.onComplete();
                                        }
                                    });

                            docker.waitContainerCmd(containerId).exec(new ResultCallback.Adapter<WaitResponse>() {
                                @SneakyThrows
                                @Override
                                public void onComplete() {
                                    solution.setFinished(LocalDateTime.now());
                                    if (!Solution.Status.KILLED.equals(solution.getStatus())) {
                                        solution.setStatus(Solution.Status.FINISHED);
                                    }
                                    docker.removeContainerCmd(containerId).withRemoveVolumes(true).exec();
                                    // TODO: remove images
                                    super.onComplete();
                                }
                            });
                            super.onComplete();
                        }
                    });
        } catch (Throwable e) {
            if (!Solution.Status.KILLED.equals(solution.getStatus())) {
                solution.setStatus(Solution.Status.ERROR);
            }
        }
    }

    public void kill(String playerId, String code, Integer solutionId) {
        solutions.stream()
                .filter(s -> playerId.equals(s.getPlayerId()))
                .filter(s -> code.equals(s.getCode()))
                .filter(s -> solutionId.equals(s.getId()))
                .findFirst()
                .ifPresentOrElse(this::kill, () -> {
                    throw new IllegalArgumentException();
                });
    }

    private void kill(Solution solution) {
        if (!solution.isActive()) {
            return;
        }
        solution.setStatus(Solution.Status.KILLED);
        if (solution.getContainerId() != null) {
            docker.killContainerCmd(solution.getContainerId()).exec();
        }
    }

    private void addDockerfile(Solution solution) {
        try {
            String relative = "dockerfiles/java/Dockerfile";

            File inSource = new File("./" + relative);
            File destination = new File(solution.getSources(), "Dockerfile");
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
            solution.setStatus(Solution.Status.ERROR);
        }
    }

    public List<Solution> getSolutions(String playerId, String code) {
        return solutions.stream()
                .filter(s -> playerId.equals(s.getPlayerId()))
                .filter(s -> code.equals(s.getCode()))
                .collect(Collectors.toList());
    }

    private Solution getSolution(String playerId, String code, Integer solutionId) {
        Solution solution = solutions.stream()
                .filter(s -> solutionId.equals(s.getId()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        if (!playerId.equals(solution.getPlayerId()) || !code.equals(solution.getCode())) {
            throw new IllegalArgumentException();
        }
        return solution;
    }

    public List<SolutionSummaryDto> getAllSolutionsSummary(String playerId, String code) {
        return getSolutions(playerId, code).stream()
                .map(SolutionSummaryDto::fromSolution)
                .sorted(Comparator.comparingInt(SolutionSummaryDto::getId))
                .collect(Collectors.toList());
    }

    public SolutionSummaryDto getSolutionSummary(Integer solutionId, String playerId, String code) {
        Solution solution = getSolution(playerId, code, solutionId);
        return SolutionSummaryDto.fromSolution(solution);
    }

    public List<String> getBuildLogs(Integer solutionId, String playerId, String code, Integer startFromLine) {
        Solution solution = getSolution(playerId, code, solutionId);
        return readFileFromLine(solution.getSources() + "/build.log", startFromLine);
    }

    public List<String> getRuntimeLogs(Integer solutionId, String playerId, String code, Integer startFromLine) {
        Solution solution = getSolution(playerId, code, solutionId);
        return readFileFromLine(solution.getSources() + "/app.log", startFromLine);
    }

    private List<String> readFileFromLine(String filePath, Integer startLine) {
        try (Stream<String> log = Files.lines(Paths.get(filePath))) {
            return log.skip(startLine).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}