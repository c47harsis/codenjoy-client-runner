package com.codenjoy.clientrunner.model;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2012 - 2022 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.*;
import org.springframework.util.Assert;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codenjoy.clientrunner.model.Solution.Status.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Solution {

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private final String playerId;
    private final String code;
    private final String gameToRun;
    private final String serverUrl;
    private final File sources;
    private final Platform platform;
    private int id;
    private LocalDateTime created;
    private LocalDateTime started;
    private LocalDateTime finished;
    private String imageId;
    private String containerId;
    private volatile Status status;

    private Solution(String playerId, String code, String gameToRun, String serverUrl, File sources, Platform platform) {
        this.playerId = playerId;
        this.code = code;
        this.gameToRun = gameToRun;
        this.serverUrl = serverUrl;
        this.status = NEW;
        this.sources = sources;
        this.created = LocalDateTime.now();
        this.platform = platform;
    }

    public static Solution from(Token token, File sources) {
        Assert.notNull(token, "Token can not be null");
        Assert.notNull(sources, "Sources can not be null");
        if (!sources.exists()) {
            throw new IllegalArgumentException("Source folder with path '" +
                    sources.getPath() + "' doesn't exist");
        }
        Platform platform = detectPlatform(sources);
        if (platform == null) {
            throw new IllegalArgumentException(
                    String.format("Solution platform not supported " +
                            "for sources: '%s'", sources));
        }
        return new Solution(token.getPlayerId(), token.getCode(),
                token.getGameToRun(),
                token.getServerUrl(),
                sources, platform);
    }

    private static Platform detectPlatform(File sources) {
        File[] files = sources.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            Platform platform = Platform.of(file.getName());
            if (platform != null) {
                return platform;
            }
        }
        return null;
    }

    public synchronized void setStatus(Status newStatus) {
        if (newStatus == null || KILLED.equals(status)) {
            return;
        }
        status = newStatus;
    }

    public boolean allows(Token token) {
        return Objects.equals(playerId, token.getPlayerId())
                && Objects.equals(code, token.getCode());
    }

    public void finish() {
        finished = LocalDateTime.now();

        if (status.getStage() <= RUNNING.getStage()) {
            status = ERROR;
        } else if (status == KILLED) {
            status = FINISHED;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        NEW(true, 0),
        COMPILING(true, 1),
        RUNNING(true, 2),
        FINISHED(false, 3),
        ERROR(false, 3),
        KILLED(false, 3);

        private final boolean active;
        private final int stage;
    }
}
