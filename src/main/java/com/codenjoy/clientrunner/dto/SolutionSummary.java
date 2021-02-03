package com.codenjoy.clientrunner.dto;

import com.codenjoy.clientrunner.model.Solution;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolutionSummary {

    private int id;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
    private LocalDateTime created;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
    private LocalDateTime started;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
    private LocalDateTime finished;

    public SolutionSummary(Solution solution) {
        created = solution.getCreated();
        finished = solution.getFinished();
        id = solution.getId();
        started = solution.getStarted();
        status = solution.getStatus().toString();
    }
}
