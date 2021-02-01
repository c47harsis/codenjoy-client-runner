package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.Solution;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DockerRunnerService {

    private final ClientServerServiceConfig config;

    private final DockerClient docker = DockerClientBuilder.getInstance().build();
    private final Set<Solution> solutions = ConcurrentHashMap.newKeySet();
    private final HostConfig hostConfig = HostConfig.newHostConfig();


    @PostConstruct
    private void init() {
        hostConfig.withCpuPeriod(config.getContainerCpuPeriod());
        hostConfig.withCpuQuota(config.getContainerCpuQuota());
        hostConfig.withMemory((long) config.getContainerMemoryLimitMB() * 1000000);
    }


    @SneakyThrows
    public String runSolution(File sources, String playerId, String code, String codenjoyUrl) {
        Solution solution = new Solution(playerId, code, codenjoyUrl, sources);

        /* TODO: try to avoid copy Dockerfile. https://docs.docker.com/engine/api/v1.41/#operation/ImageBuild */
        addDockerfile(solution);
        solutions.add(solution);

        if (Solution.Status.KILLED.equals(solution.getStatus())) {
            return "nothing";
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
        return "успех";
    }


    public void killAll(String playerId, String code) {
        solutions.stream()
                .filter(s -> playerId.equals(s.getPlayerId()) && code.equals(s.getCode()))
                .forEach(this::kill0);
    }


    public void kill(String playerId, String code, Integer solutionId) {
        Solution solution = solutions.stream()
                .filter(s -> playerId.equals(s.getPlayerId()) && code.equals(s.getCode()) && solutionId.equals(s.getId()))
                .findFirst()
                .orElseThrow(RuntimeException::new);
        kill0(solution);
    }


    private void kill0(Solution solution) {
        if (Solution.Status.FINISHED.equals(solution.getStatus())) {
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
                .filter(s -> playerId.equals(s.getPlayerId()) && code.equals(s.getCode()))
                .collect(Collectors.toList());
    }
}