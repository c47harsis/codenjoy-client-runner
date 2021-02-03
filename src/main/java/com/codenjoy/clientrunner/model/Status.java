package com.codenjoy.clientrunner.model;

public enum Status {

    NEW, COMPILING, RUNNING, FINISHED, ERROR, KILLED;

    public boolean isActive() {
        switch (this) {
            case NEW:
            case COMPILING:
            case RUNNING:
                return true;
            default:
                return false;
        }
    }
}
