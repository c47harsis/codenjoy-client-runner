package com.codenjoy.clientrunner.dto;

import lombok.Data;

@Data
public class CheckRequest {
    private String serverUrl;
    private String repo;
}
