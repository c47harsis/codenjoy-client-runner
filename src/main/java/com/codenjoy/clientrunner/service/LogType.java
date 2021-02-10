package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.model.Solution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.codenjoy.clientrunner.model.Solution.Status.COMPILING;
import static com.codenjoy.clientrunner.model.Solution.Status.RUNNING;

@Getter
@RequiredArgsConstructor
public enum LogType {

    BUILD("build.log", COMPILING),
    RUNTIME("app.log", RUNNING);

    private final String filename;
    private final Solution.Status status;

    public boolean existsWhen(Solution.Status status) {
        return this.status.getStage() <= status.getStage();
    }
}
