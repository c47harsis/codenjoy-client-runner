package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.dto.Solution;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@AllArgsConstructor
public class DockerRunnerService {

    private final DockerClient docker = DockerClientBuilder.getInstance().build();
    private final Set<Solution> solutions = ConcurrentHashMap.newKeySet();
    private final AtomicInteger nextSolutionId = new AtomicInteger(0);
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public Solution createSolution(File sources, String playerId, String code, String codenjoyUrl) {
        Solution solution = new Solution();
        solution.setId(nextSolutionId.incrementAndGet());
        solution.setSources(sources);
        solution.setPlayerId(playerId);
        solution.setCode(code);
        solution.setCodenjoyUrl(codenjoyUrl);
        solution.setStatus(new AtomicReference<>(Solution.Status.NEW));
        return solution;
    }

    public String runSolution(Solution solution) {

        /* TODO: try to avoid copy Dockerfile. https://docs.docker.com/engine/api/v1.41/#operation/ImageBuild */
        addDockerfile(solution);
        solutions.add(solution);

        pool.execute(() -> {
            try {
                if (!solution.getStatus().compareAndSet(Solution.Status.NEW, Solution.Status.COMPILING)) {
                    return;
                }
                String imageId = buildImage(solution.getSources(), solution.getCodenjoyUrl());
                solution.setImageId(imageId);

                String containerId = createContainer(solution.getImageId());
                solution.setContainerId(containerId);

                if (!solution.getStatus().compareAndSet(Solution.Status.COMPILING, Solution.Status.RUNNING)) {
                    return;
                }
                solution.setStarted(LocalDateTime.now());
                startContainer(solution.getContainerId());

                logToFile(solution.getContainerId(), solution.getSources().getPath() + "/app.log");
                solution.setFinished(LocalDateTime.now());
                solution.getStatus().set(Solution.Status.FINISHED);

            } catch (Throwable ex) {
                solution.getStatus().set(Solution.Status.ERROR);
            } finally {
                cleanup(solution);
            }
        });

        return "успех";
    }

    private String buildImage(File sources, String codenjoyUrl) {
        return docker.buildImageCmd(sources)
                .withBuildArg("CODENJOY_URL", codenjoyUrl)
                .exec(new BuildImageResultCallback())
                .awaitImageId();
    }

    private String createContainer(String imageId) {
        return docker.createContainerCmd(imageId)
                .exec()
                .getId();
    }

    private String startContainer(String containerId) {
        docker.startContainerCmd(containerId)
                .exec();
        return containerId;
    }

    private void logToFile(String containerId, String logPath) throws IOException, InterruptedException {
        List<String> logs = new ArrayList<>();
        docker.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .withTailAll()
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame object) {
                        logs.add(object.toString());
                    }
                }).awaitCompletion();

        FileWriter fileWriter = new FileWriter(logPath);
        for (String line : logs) {
            fileWriter.write(line + System.lineSeparator());
        }
        fileWriter.close();
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
        AtomicReference<Solution.Status> status = solution.getStatus();
        Solution.Status lastStatus = status.getAndSet(Solution.Status.KILLED);
        if (Solution.Status.FINISHED.equals(lastStatus)) {
            status.set(lastStatus);
            return;
        }
        if (solution.getContainerId() != null) {
            docker.killContainerCmd(solution.getContainerId()).exec();
        }
    }

    private void cleanup(Solution solution) {
        if (solution.getContainerId() != null) {
            docker.removeContainerCmd(solution.getContainerId()).exec();
        }
        // TODO: Remove images
    }

    public void inspect() {
        solutions.forEach(System.out::println);
    }


    private void addDockerfile(Solution solution) {
        try {
            FileUtils.copyFile(
                    new File("./dockerfiles/java/Dockerfile"),
                    new File(solution.getSources(), "Dockerfile")
            );
        } catch (IOException e) {
            log.error("Can not add Dockerfile to solution with id: {}", solution.getId());
            solution.getStatus().set(Solution.Status.ERROR);
        }
    }
}