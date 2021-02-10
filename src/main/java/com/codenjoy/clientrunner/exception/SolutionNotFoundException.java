package com.codenjoy.clientrunner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SolutionNotFoundException extends RuntimeException {
    public SolutionNotFoundException(int id) {
        super("Solution with id:" + id + " not found!");
    }
}
