package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.ShortSolutionDto;
import com.codenjoy.clientrunner.dto.Solution;
import com.codenjoy.clientrunner.dto.SolutionDto;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
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
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(solutionDto.getCodenjoyUrl());
        String playerId = playerIdAndCode.first;
        String code = playerIdAndCode.second;

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

    private Pair<String, String> extractPlayerIdAndCode(String url) {
        Pattern serverUrlPattern = Pattern.compile(config.getCodenjoyUrlRegex());
        Matcher matcher = serverUrlPattern.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Given invalid server URL: '%s' is not match '%s'",
                            url, config.getCodenjoyUrlRegex()));
        }
        return new Pair<>(matcher.group(1), matcher.group(2));
    }

    public void killSolution(String url, Integer solutionId) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(url);
        killSolution(playerIdAndCode.first, playerIdAndCode.second, solutionId);
    }

    public void killSolution(String playerId, String code, Integer solutionId) {
        dockerRunnerService.kill(playerId, code, solutionId);
    }

    public List<ShortSolutionDto> getAllSolutions(String url) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(url);
        return getAllSolutions(playerIdAndCode.first, playerIdAndCode.second);
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
        }).sorted(Comparator.comparingInt(ShortSolutionDto::getId)).collect(Collectors.toList());
    }

    public List<String> getLogs(String url, Integer solutionId) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(url);
        return getLogs(playerIdAndCode.first, playerIdAndCode.second, solutionId);
    }

    public List<String> getLogs(String playerId, String code, Integer solutionId) {
        Solution solution = dockerRunnerService.getSolutions(playerId, code).stream()
                .filter(s -> solutionId.equals(s.getId()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        try {
            return Files.readAllLines(Paths.get(solution.getSources() + "/app.log"));
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }


    public ShortSolutionDto getSol(String url, Integer solutionId) {
        Pair<String, String> playerIdAndCode = extractPlayerIdAndCode(url);
        return getSol(playerIdAndCode.first, playerIdAndCode.second, solutionId);
    }
    public ShortSolutionDto getSol(String playerId, String code, Integer solutionId) {
        Solution s = dockerRunnerService.getSolutions(playerId, code).stream()
                .filter(sol -> solutionId.equals(sol.getId()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        ShortSolutionDto res = new ShortSolutionDto();
        res.setCreated(s.getCreated());
        res.setFinished(s.getFinished());
        res.setId(s.getId());
        res.setStarted(s.getStarted());
        res.setStatus(s.getStatus().toString());
        return res;
    }

    @AllArgsConstructor
    private final static class Pair<F, S> {
        private final F first;
        private final S second;
    }
}
