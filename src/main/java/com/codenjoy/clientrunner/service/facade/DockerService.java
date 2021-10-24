package com.codenjoy.clientrunner.service.facade;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerService {

    public static final String SERVER_URL = "SERVER_URL";
    public static final String GAME_TO_RUN = "GAME_TO_RUN";

    private final DockerClientConfig dockerClientConfig = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .build();

    private final DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(dockerClientConfig.getDockerHost())
            .sslConfig(dockerClientConfig.getSSLConfig())
            .build();

    private final DockerClient docker = DockerClientBuilder.getInstance()
            .withDockerHttpClient(dockerHttpClient)
            .build();

    @PostConstruct
    protected void init() {
        try {
            docker.pingCmd().exec();
        } catch (RuntimeException e) {
            log.error("Docker not found");
        }
    }

    public void killContainer(String containerId) {
        if (isContainerRunning(containerId)) {
            docker.killContainerCmd(containerId)
                    .exec();
        }
    }

    public boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse response = docker.inspectContainerCmd(containerId)
                    .exec();
            return Optional.ofNullable(response)
                    .map(InspectContainerResponse::getState)
                    .map(InspectContainerResponse.ContainerState::getRunning)
                    .orElse(false);
        } catch (NotFoundException e) {
            return false;
        }
    }

    public void removeContainer(String containerId) {
        try {
            docker.removeContainerCmd(containerId)
                    .withRemoveVolumes(true)
                    .exec();
        } catch (NotFoundException e) {
            // do nothing, container already removed
        }
    }

    public void waitContainer(String containerId, Runnable onComplete) {
        docker.waitContainerCmd(containerId)
                .exec(new ResultCallback.Adapter<>() {
                    @SneakyThrows
                    @Override
                    public void onComplete() {
                        onComplete.run();
                        super.onComplete();
                    }
                });
    }

    public void startContainer(String containerId) {
        docker.startContainerCmd(containerId).exec();
    }

    public String createContainer(String imageId, HostConfig hostConfig) {
        return docker.createContainerCmd(imageId)
                .withHostConfig(hostConfig)
                .exec().getId();
    }

    public void buildImage(File sources, String gameToRun, String serverUrl, LogWriter writer, Consumer<String> onCompete) {
        docker.buildImageCmd(sources)
                .withBuildArg(GAME_TO_RUN, gameToRun)
                .withBuildArg(SERVER_URL, serverUrl)
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

                        onCompete.accept(imageId);
                        super.onComplete();
                    }
                });
    }

    public void logContainer(String containerId, LogWriter writer) {
        docker.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .withTailAll()
                .exec(new ResultCallback.Adapter<>() {

                    @Override
                    public void onNext(Frame object) {
                        writer.write(object);
                    }

                    @Override
                    public void onComplete() {
                        writer.close();
                        super.onComplete();
                    }
                });
    }
}
