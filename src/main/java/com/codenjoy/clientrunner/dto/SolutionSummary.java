package com.codenjoy.clientrunner.dto;

import com.codenjoy.clientrunner.model.Solution;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolutionSummary {
    private Integer id;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
    private LocalDateTime created;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
    private LocalDateTime started;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
    private LocalDateTime finished;

    public static SolutionSummary fromSolution(Solution solution) {
        SolutionSummary summary = new SolutionSummary();
        summary.setCreated(solution.getCreated());
        summary.setFinished(solution.getFinished());
        summary.setId(solution.getId());
        summary.setStarted(solution.getStarted());
        summary.setStatus(solution.getStatus().toString());
        return summary;
    }
}
