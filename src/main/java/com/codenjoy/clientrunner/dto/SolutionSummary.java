package com.codenjoy.clientrunner.dto;

import com.codenjoy.clientrunner.model.Solution;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolutionSummary {

    public static final String TIME_PATTERN = "hh:mm:ss";

    private int id;
    private String status;

    @JsonFormat(shape = STRING, pattern = TIME_PATTERN)
    private LocalDateTime created;

    @JsonFormat(shape = STRING, pattern = TIME_PATTERN)
    private LocalDateTime started;

    @JsonFormat(shape = STRING, pattern = TIME_PATTERN)
    private LocalDateTime finished;

    public SolutionSummary(Solution solution) {
        created = solution.getCreated();
        finished = solution.getFinished();
        id = solution.getId();
        started = solution.getStarted();
        status = solution.getStatus().toString();
    }
}
