package com.codenjoy.clientrunner.service;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2012 - 2022 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.LogType;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ClientRunnerService {

    private final ClientServerServiceConfig config;
    private final GitService git;
    private final SolutionManager solutionManager;

    public void checkSolution(CheckRequest request) {
        Token token = parse(request.getServerUrl());
        File directory = getSolutionDirectory(token);

        // TODO: async
        git.clone(request.getRepo(), directory)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Can not clone repository: " + request.getRepo()));

        solutionManager.runSolution(token, directory);
    }

    public void killSolution(String serverUrl, int solutionId) {
        Token token = parse(serverUrl);
        solutionManager.kill(token, solutionId);
    }

    public List<SolutionSummary> getAllSolutionsSummary(String serverUrl) {
        Token token = parse(serverUrl);
        return solutionManager.getAllSolutionSummary(token);
    }

    public SolutionSummary getSolutionSummary(String serverUrl, int solutionId) {
        Token token = parse(serverUrl);
        return solutionManager.getSolutionSummary(token, solutionId);
    }

    public List<String> getLogs(String serverUrl, int solutionId, LogType logType, int offset) {
        Token token = parse(serverUrl);
        return solutionManager.getLogs(token, solutionId, logType, offset);
    }

    private Token parse(String serverUrl) {
        return Token.from(serverUrl, config.getServerRegex());
    }

    private File getSolutionDirectory(Token token) {
        return new File(String.format("%s/%s/%s/%s",
                config.getSolutions().getPath(),
                token.getPlayerId(), token.getCode(),
                now()));
    }

    private String now() {
        String pattern = config.getSolutions().getPattern();
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(pattern));
    }
}
