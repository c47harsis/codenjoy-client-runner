package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.SolutionDto;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ClientServerService implements CommandLineRunner {

    private final ClientServerServiceConfig config;
    private final GitService gitService;
    private final DockerRunnerService dockerRunnerService;

    private final Pattern serverUrlPattern = Pattern.compile(config.getCodenjoyUrlRegex());


    public void checkSolution(SolutionDto solutionDto) {
        Matcher matcher = serverUrlPattern.matcher(solutionDto.getCodenjoyUrl());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Given invalid server URL: %s", solutionDto.getCodenjoyUrl()));
        }

        String playerId = matcher.group(1);
        String code = matcher.group(2);

        File directory = new File(String.format("./%s/%s/%s/%s", config.getSolutionsFolderPath(), playerId, code,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy 'T'HH_mm_ss"))));

        Git repo = gitService.clone(solutionDto.getRepoUrl(), directory);

        if (repo == null) {
            // TODO: handle absent of repo
            return;
        }

        String containerId = dockerRunnerService.runSolution(directory, solutionDto.getCodenjoyUrl());
        System.out.println(containerId);
    }


    @Override
    public void run(String... args) {
        SolutionDto solutionDto = new SolutionDto();
        solutionDto.setRepoUrl("https://github.com/c47harsis/testrepo.git");
        solutionDto.setCodenjoyUrl("https://dojorena.io/codenjoy-contest/board/player/dojorena146?code=8433729297737152765");
        checkSolution(solutionDto);
    }
}
