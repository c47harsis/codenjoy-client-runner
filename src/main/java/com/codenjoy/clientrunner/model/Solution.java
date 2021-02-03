package com.codenjoy.clientrunner.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codenjoy.clientrunner.model.Status.KILLED;
import static com.codenjoy.clientrunner.model.Status.NEW;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Solution {

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private int id;
    private String playerId;
    private String code;
    private String server;
    public volatile Status status;
    private LocalDateTime created;
    private LocalDateTime started;
    private LocalDateTime finished;
    private String containerId;
    private String imageId;
    private File sources;

    public Solution(Server server, File sources) {
        this.id = idCounter.incrementAndGet();
        this.playerId = server.getPlayerId();
        this.code = server.getCode();
        this.server = server.getServer();
        this.status = NEW;
        this.sources = sources;
        this.created = LocalDateTime.now();
    }

    public synchronized void setStatus(Status newStatus) {
        if (newStatus == null || KILLED.equals(status)) {
            return;
        }
        status = newStatus;
    }

}

