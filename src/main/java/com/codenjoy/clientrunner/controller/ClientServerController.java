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

    private final ClientServerService clientServerService;

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    void checkNewSolution(@RequestBody CheckRequest checkRequest) {
        clientServerService.checkSolution(checkRequest);
    }

    @GetMapping("/stop")
    @ResponseStatus(HttpStatus.OK)
    void stopSolution(@RequestParam String codenjoyUrl, @RequestParam Integer solutionId) {
        clientServerService.killSolution(codenjoyUrl, solutionId);
    }

    @GetMapping("/all")
    ResponseEntity<?> getAllSolutions(@RequestParam String codenjoyUrl) {
        return ok(clientServerService.getAllSolutionsSummary(codenjoyUrl));
    }

    @GetMapping("/summary")
    ResponseEntity<?> getSolutionSummary(@RequestParam Integer solutionId, @RequestParam String codenjoyUrl) {
        return ok(clientServerService.getSolutionSummary(codenjoyUrl, solutionId));
    }

    @GetMapping("/runtime_logs")
    ResponseEntity<?> getRuntimeLogs(@RequestParam Integer solutionId, @RequestParam String codenjoyUrl,
                                     @RequestParam(defaultValue = "0") Integer startFromLine) {
        return ok(clientServerService.getRuntimeLogs(codenjoyUrl, solutionId, startFromLine));
    }

    @GetMapping("/build_logs")
    ResponseEntity<?> getBuildLogs(@RequestParam Integer solutionId, @RequestParam String codenjoyUrl,
                                   @RequestParam(defaultValue = "0") Integer startFromLine) {
        return ok(clientServerService.getBuildLogs(codenjoyUrl, solutionId, startFromLine));
    }
}
