package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.SolutionDto;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
public class ClientServerService implements CommandLineRunner {

    private final ClientServerServiceConfig clientServerServiceConfig;
    private final GitService gitService;
    private final DockerRunnerService dockerRunnerService;

    public void checkSolution(SolutionDto solutionDto) {
        File directory = new File(
                MessageFormat.format(
                        "{0}/{1}/{2}/{3}",
                        clientServerServiceConfig.getSolutionsFolderPath(),
                        solutionDto.getPlayerId(),
                        solutionDto.getCode(),
                        LocalTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss")))
        );

        Git repo = gitService.clone(solutionDto.getRepoUri(), directory);
        if (repo == null) {
            return;
        }
        String containerId = dockerRunnerService.runSolution(directory, solutionDto.getPlayerId());
        System.out.println(containerId);
    }

    @Override
    public void run(String... args) throws Exception {
        SolutionDto solutionDto = new SolutionDto();
        solutionDto.setCode("123412341234");
        solutionDto.setRepoUri("https://github.com/c47harsis/testrepo.git");
        solutionDto.setPlayerId("1234vasya");
        checkSolution(solutionDto);
    }
}
