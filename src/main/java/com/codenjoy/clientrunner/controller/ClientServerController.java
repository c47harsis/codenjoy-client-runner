package com.codenjoy.clientrunner.controller;

import com.codenjoy.clientrunner.dto.SolutionDto;
import com.codenjoy.clientrunner.service.ClientServerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ClientServerController {

    private final ClientServerService clientServerService;

    @PostMapping
    void checkSolution(@RequestBody SolutionDto solutionDto) {
        clientServerService.checkSolution(solutionDto);
    }
}
