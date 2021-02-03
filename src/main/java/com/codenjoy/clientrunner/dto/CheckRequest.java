package com.codenjoy.clientrunner.dto;

import lombok.Data;

@Data
public class CheckRequest {
    private String server;
    private String repo;
}
