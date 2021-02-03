package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.model.Solution;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DockerService {

    private final DockerClient docker = DockerClientBuilder.getInstance().build();

    public void killContainer(Solution solution) {
        docker.killContainerCmd(solution.getContainerId())
                .exec();
    }

    public void removeContainer(Solution solution) {
        docker.removeContainerCmd(solution.getContainerId())
                .withRemoveVolumes(true)
                .exec();
    }

    public void waitContainer(Solution solution, Runnable onComplete) {
        docker.waitContainerCmd(solution.getContainerId())
                .exec(new ResultCallback.Adapter<>() {
                    @SneakyThrows
                    @Override
                    public void onComplete() {
                        onComplete.run();
                        super.onComplete();
                    }
                });
    }
}
