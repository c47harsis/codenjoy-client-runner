package com.codenjoy.clientrunner.model;

public enum Status {

    NEW(true),
    COMPILING(true),
    RUNNING(true),
    FINISHED(false),
    ERROR(false),
    KILLED(false);

    private boolean active;

    Status(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
