package com.codenjoy.clientrunner.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class DockerRunnerService {

    private final DockerClient docker = DockerClientBuilder.getInstance().build();
    private final Set<String> runningContainers = new HashSet<>();


    public String runSolution(File solutionSources, String playerId) {

        addDockerfile(solutionSources);

        String imageId = docker.buildImageCmd(solutionSources)
                .withNoCache(false)
                .exec(new BuildImageResultCallback())
                .awaitImageId();

        String containerId = docker.createContainerCmd(imageId)
                .withName(LocalTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss")) + "_" + playerId)
                .exec()
                .getId();

        docker.startContainerCmd(containerId).exec();
        runningContainers.add(containerId);
        return containerId;
    }


    public void stopSolution(String containerId) {
        docker.stopContainerCmd(containerId).exec();
        docker.removeContainerCmd(containerId).exec();
        runningContainers.remove(containerId);
    }


    public void stopAll() {
        runningContainers.forEach(this::stopSolution);
    }


    private void addDockerfile(File sources) {
        File dockerfileFrom = new File("./dockerfiles/java/Dockerfile");
        File dockerfileTo = new File(sources, "Dockerfile");

        try {
            FileUtils.copyFile(dockerfileFrom, dockerfileTo);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }
}