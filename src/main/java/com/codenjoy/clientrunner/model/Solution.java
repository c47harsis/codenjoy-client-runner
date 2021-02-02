package com.codenjoy.clientrunner.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Solution {
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private Integer id;
    private String playerId;
    private String code;
    private String codenjoyUrl;
    private volatile Status status;
    private LocalDateTime created;
    private LocalDateTime started;
    private LocalDateTime finished;
    private String containerId;
    private String imageId;
    private File sources;

    public Solution(String playerId, String code, String codenjoyUrl, File sources) {
        this.id = Solution.idCounter.incrementAndGet();
        this.playerId = playerId;
        this.code = code;
        this.codenjoyUrl = codenjoyUrl;
        this.status = Status.NEW;
        this.sources = sources;
        this.created = LocalDateTime.now();
    }

    public boolean isActive() {
        switch (status) {
            case NEW:
            case COMPILING:
            case RUNNING:
                return true;
            default:
                return false;
        }
    }

    public synchronized void setStatus(Status newStatus) {
        if (newStatus == null || Status.KILLED.equals(status)) {
            return;
        }
        status = newStatus;
    }

    public enum Status {
        NEW, COMPILING, RUNNING, FINISHED, ERROR, KILLED
    }
}

