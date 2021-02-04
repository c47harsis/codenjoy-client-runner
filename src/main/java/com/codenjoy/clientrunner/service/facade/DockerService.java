package com.codenjoy.clientrunner.service.facade;

import com.codenjoy.clientrunner.model.Solution;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class DockerService {

    public static final String SERVER_PARAMETER = "CODENJOY_URL";

    private final DockerClient docker = DockerClientBuilder.getInstance().build();

    @PostConstruct
    protected void init() {
        try {
            docker.pingCmd().exec();
        } catch (RuntimeException e) {
            log.error("Docker not found");
        }
    }

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

    public void startContainer(Solution solution) {
        docker.startContainerCmd(solution.getContainerId()).exec();
    }

    public String createContainer(String imageId, HostConfig hostConfig) {
        return docker.createContainerCmd(imageId)
                .withHostConfig(hostConfig)
                .exec().getId();
    }

    public void buildImage(Solution solution, LogWriter writer, Consumer<String> onCompete) {
        docker.buildImageCmd(solution.getSources())
                .withBuildArg(SERVER_PARAMETER, solution.getServerUrl())
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

    public void logContainer(Solution solution, LogWriter writer) {
        docker.logContainerCmd(solution.getContainerId())
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
