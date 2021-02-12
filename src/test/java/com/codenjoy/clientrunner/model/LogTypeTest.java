package com.codenjoy.clientrunner.model;

import org.testng.annotations.Test;

import static com.codenjoy.clientrunner.model.LogType.*;
import static com.codenjoy.clientrunner.model.Solution.Status.*;
import static org.testng.Assert.*;

public class LogTypeTest {

    @Test
    public void shouldCheckBuildLogFile_forAllStatuses() {
        boolean exists = true;

        assertEquals(BUILD.existsWhen(NEW), false);
        assertEquals(BUILD.existsWhen(COMPILING), exists);
        assertEquals(BUILD.existsWhen(RUNNING), exists);
        assertEquals(BUILD.existsWhen(FINISHED), exists);
        assertEquals(BUILD.existsWhen(ERROR), exists);
        assertEquals(BUILD.existsWhen(KILLED), exists);

        assertEquals(RUNTIME.existsWhen(NEW), false);
        assertEquals(RUNTIME.existsWhen(COMPILING), false);
        assertEquals(RUNTIME.existsWhen(RUNNING), exists);
        assertEquals(RUNTIME.existsWhen(FINISHED), exists);
        assertEquals(RUNTIME.existsWhen(ERROR), exists);
        assertEquals(RUNTIME.existsWhen(KILLED), exists);
    }

    @Test
    public void shouldGetLogFilename() {
        assertEquals(BUILD.getFilename(), "build.log");
        assertEquals(RUNTIME.getFilename(), "app.log");
    }

}