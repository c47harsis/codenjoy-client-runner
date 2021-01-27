package com.codenjoy.clientrunner.controller;

import com.codenjoy.clientrunner.dto.SolutionDto;
import com.codenjoy.clientrunner.service.ClientServerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ClientServerController {

    private final ClientServerService clientServerService;

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    void checkSolution(@RequestBody SolutionDto solutionDto) {
        clientServerService.checkSolution(solutionDto);
    }

    @GetMapping("/stop")
    @ResponseStatus(HttpStatus.OK)
    void kill(@RequestParam String playerId, @RequestParam String code, @RequestParam Integer solutionId) {
        clientServerService.killSolution(playerId, code, solutionId);
    }

    @GetMapping("/get_all")
    ResponseEntity<?> getAllSolutions(@RequestParam String playerId, @RequestParam String code) {
        return ResponseEntity.ok(clientServerService.getAllSolutions(playerId, code));
    }

    @GetMapping("/get_logs")
    ResponseEntity<?> getLogs(@RequestParam String playerId, @RequestParam String code, @RequestParam Integer solutionId) {
        return ResponseEntity.ok(clientServerService.getLogs(playerId, code, solutionId));
    }
}
