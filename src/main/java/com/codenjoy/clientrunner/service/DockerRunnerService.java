package com.codenjoy.clientrunner.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class DockerRunnerService {

    private final DockerClient docker = DockerClientBuilder.getInstance().build();
    private final Set<String> runningContainers = new HashSet<>();


    public String runSolution(File sources, String codenjoyURL) {

        /* TODO: try to avoid copy Dockerfile. https://docs.docker.com/engine/api/v1.41/#operation/ImageBuild */
        addDockerfile(sources);

        String imageId = docker.buildImageCmd(sources)
                .withBuildArg("CODENJOY_URL", codenjoyURL)
                .exec(new BuildImageResultCallback())
                .awaitImageId();

        String containerId = docker.createContainerCmd(imageId)
                .exec()
                .getId();

        docker.startContainerCmd(containerId)
                .exec();

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
        try {
            FileUtils.copyFile(
                    new File("./dockerfiles/java/Dockerfile"),
                    new File(sources, "Dockerfile")
            );
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }
}