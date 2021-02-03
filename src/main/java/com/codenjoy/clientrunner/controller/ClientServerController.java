package com.codenjoy.clientrunner.controller;

import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.service.ClientServerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.*;

@RestController("/solutions")
@AllArgsConstructor
public class ClientServerController {

    private final ClientServerService service;

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    void checkNewSolution(@RequestBody CheckRequest request) {
        service.checkSolution(request);
    }

    @GetMapping("/stop")
    @ResponseStatus(HttpStatus.OK)
    void stopSolution(@RequestParam String server, @RequestParam Integer solutionId) {
        service.killSolution(server, solutionId);
    }

    @GetMapping("/all")
    ResponseEntity<?> getAllSolutions(@RequestParam String server) {
        return ok(service.getAllSolutionsSummary(server));
    }

    @GetMapping("/summary")
    ResponseEntity<?> getSolutionSummary(@RequestParam Integer solutionId, @RequestParam String server) {
        return ok(service.getSolutionSummary(server, solutionId));
    }

    @GetMapping("/runtime_logs")
    ResponseEntity<?> getRuntimeLogs(@RequestParam Integer solutionId, @RequestParam String server,
                                     @RequestParam(defaultValue = "0") Integer offset) {
        return ok(service.getRuntimeLogs(server, solutionId, offset));
    }

    @GetMapping("/build_logs")
    ResponseEntity<?> getBuildLogs(@RequestParam Integer solutionId, @RequestParam String server,
                                   @RequestParam(defaultValue = "0") Integer offset) {
        return ok(service.getBuildLogs(server, solutionId, offset));
    }
}
