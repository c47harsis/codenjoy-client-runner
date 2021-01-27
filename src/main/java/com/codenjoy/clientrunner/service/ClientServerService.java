package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.ShortSolutionDto;
import com.codenjoy.clientrunner.dto.Solution;
import com.codenjoy.clientrunner.dto.SolutionDto;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClientServerService  {

    private final ClientServerServiceConfig config;
    private final GitService gitService;
    private final DockerRunnerService dockerRunnerService;

    public void checkSolution(SolutionDto solutionDto) {
        Pattern serverUrlPattern = Pattern.compile(config.getCodenjoyUrlRegex());
        Matcher matcher = serverUrlPattern.matcher(solutionDto.getCodenjoyUrl());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Given invalid server URL: %s", solutionDto.getCodenjoyUrl()));
        }

        String playerId = matcher.group(1);
        String code = matcher.group(2);

        File directory = new File(String.format("./%s/%s/%s/%s", config.getSolutionFolderPath(), playerId, code,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(config.getSolutionFolderPattern()))));

        Git repo = gitService.clone(solutionDto.getRepoUrl(), directory);

        if (repo == null) {
            // TODO: handle absent of repo
            return;
        }

        String containerId = dockerRunnerService.runSolution(directory, playerId, code, solutionDto.getCodenjoyUrl());
        System.out.println(containerId);
    }


//    @SneakyThrows
//    @Override
//    public void run(String... args) {
//        new Thread(() -> {
//            while (true) {
//                dockerRunnerService.inspect();
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("\n------\n");
//            }
//        }).start();
//
//        SolutionDto solutionDto = new SolutionDto();
//        solutionDto.setRepoUrl("https://github.com/c47harsis/testrepo.git");
//        solutionDto.setCodenjoyUrl("https://dojorena.io/codenjoy-contest/board/player/dojorena146?code=8433729297737152765");
//
//        checkSolution(solutionDto);
//        dockerRunnerService.inspect();
//
//        Thread.sleep(5000);
//        checkSolution(solutionDto);
//        dockerRunnerService.inspect();
//
//        Thread.sleep(10000);
//        checkSolution(solutionDto);
////
//        dockerRunnerService.killAll("dojorena146", "8433729297737152765");
////
////      dockerRunnerService.runSolution(new File("./solutions/dojorena146/8433729297737152765/21-01-2021 T20_28_40"), "https://dojorena.io/codenjoy-contest/board/player/dojorena146?code=8433729297737152765");
//    }

    public void killSolution(String playerId, String code, Integer solutionId) {
        dockerRunnerService.kill(playerId, code, solutionId);
    }

    public List<ShortSolutionDto> getAllSolutions(String playerId, String code) {
        return dockerRunnerService.getSolutions(playerId, code).stream().map(s -> {
            ShortSolutionDto res = new ShortSolutionDto();
            res.setCreated(s.getCreated());
            res.setFinished(s.getFinished());
            res.setId(s.getId());
            res.setStarted(s.getStarted());
            res.setStatus(s.getStatus().toString());
            return res;
        }).collect(Collectors.toList());
    }
}
