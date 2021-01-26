package com.codenjoy.clientrunner.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Data
@EqualsAndHashCode
@ToString
public class Solution {
    private Integer id;
    private String playerId;
    private String code;
    private String codenjoyUrl;
    private AtomicReference<Status> status;
    private LocalDateTime started;
    private LocalDateTime finished;

    @JsonIgnore
    private String containerId;

    @JsonIgnore
    private String imageId;

    @JsonIgnore
    private File sources;

    public enum Status {
        NEW, COMPILING, RUNNING, FINISHED, ERROR, KILLED
    }
}
