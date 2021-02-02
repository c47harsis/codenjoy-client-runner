package com.codenjoy.clientrunner.dto;

import lombok.Data;

@Data
public class CheckRequest {
    private String codenjoyUrl;
    private String repoUrl;
}
