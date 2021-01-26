package com.codenjoy.clientrunner.controller;

import com.codenjoy.clientrunner.dto.SolutionDto;
import com.codenjoy.clientrunner.service.ClientServerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.QueryParam;

@RestController
@AllArgsConstructor
public class ClientServerController {

    private final ClientServerService clientServerService;

    @PostMapping
    void checkSolution(@RequestBody SolutionDto solutionDto) {
        clientServerService.checkSolution(solutionDto);
    }

//    @GetMapping
//    ResponseEntity<?> getAllSolutions(@RequestParam String code) {
//        clientServerService.getAllSolutions(code);
//    }
//
//    @GetMapping
//    ResponseEntity<?> kill(@RequestParam String code, @RequestParam String solutionId) {
//        clientServerService.kill(code, solutionId);
//    }
}
