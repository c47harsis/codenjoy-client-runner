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
    void stopSolution(@RequestParam String codenjoyUrl, @RequestParam Integer solutionId) {
        service.killSolution(codenjoyUrl, solutionId);
    }

    @GetMapping("/all")
    ResponseEntity<?> getAllSolutions(@RequestParam String codenjoyUrl) {
        return ok(service.getAllSolutionsSummary(codenjoyUrl));
    }

    @GetMapping("/summary")
    ResponseEntity<?> getSolutionSummary(@RequestParam Integer solutionId, @RequestParam String codenjoyUrl) {
        return ok(service.getSolutionSummary(codenjoyUrl, solutionId));
    }

    @GetMapping("/runtime_logs")
    ResponseEntity<?> getRuntimeLogs(@RequestParam Integer solutionId, @RequestParam String codenjoyUrl,
                                     @RequestParam(defaultValue = "0") Integer startFromLine) {
        return ok(service.getRuntimeLogs(codenjoyUrl, solutionId, startFromLine));
    }

    @GetMapping("/build_logs")
    ResponseEntity<?> getBuildLogs(@RequestParam Integer solutionId, @RequestParam String codenjoyUrl,
                                   @RequestParam(defaultValue = "0") Integer startFromLine) {
        return ok(service.getBuildLogs(codenjoyUrl, solutionId, startFromLine));
    }
}
