package com.codenjoy.clientrunner.controller;

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

import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.model.LogType;
import com.codenjoy.clientrunner.service.ClientRunnerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController("/solutions")
@AllArgsConstructor
public class ClientRunnerController {

    private final ClientRunnerService service;

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    void checkNewSolution(@RequestBody CheckRequest request) {
        service.checkSolution(request);
    }

    @GetMapping("/stop")
    @ResponseStatus(HttpStatus.OK)
    void stopSolution(@RequestParam String serverUrl, @RequestParam int solutionId) {
        service.killSolution(serverUrl, solutionId);
    }

    @GetMapping("/all")
    ResponseEntity<?> getAllSolutions(@RequestParam String serverUrl) {
        return ok(service.getAllSolutionsSummary(serverUrl));
    }

    @GetMapping("/summary")
    ResponseEntity<?> getSolutionSummary(@RequestParam int solutionId, @RequestParam String serverUrl) {
        return ok(service.getSolutionSummary(serverUrl, solutionId));
    }

    @GetMapping("/runtime_logs")
    ResponseEntity<?> getRuntimeLogs(@RequestParam int solutionId, @RequestParam String serverUrl,
                                     @RequestParam(defaultValue = "0") int offset) {
        return ok(service.getLogs(serverUrl, solutionId, LogType.RUNTIME, offset));
    }

    @GetMapping("/build_logs")
    ResponseEntity<?> getBuildLogs(@RequestParam int solutionId, @RequestParam String serverUrl,
                                   @RequestParam(defaultValue = "0") int offset) {
        return ok(service.getLogs(serverUrl, solutionId, LogType.BUILD, offset));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
