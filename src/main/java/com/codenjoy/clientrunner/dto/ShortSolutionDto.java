package com.codenjoy.clientrunner.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortSolutionDto {
    private Integer id;
    private String status;
    private LocalDateTime created;
    private LocalDateTime started;
    private LocalDateTime finished;
}
