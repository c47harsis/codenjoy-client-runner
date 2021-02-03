package com.codenjoy.clientrunner.service.facade;

import com.codenjoy.clientrunner.model.Solution;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class LogWriter {

    public static final String BUILD_LOG = "/build.log";
    public static final String APP_LOG = "/app.log";

    private final Writer writer;

    @SneakyThrows
    public LogWriter(Solution solution, boolean isBuild) {
        String file = isBuild ? BUILD_LOG : APP_LOG;
        writer = new BufferedWriter(new FileWriter(solution.getSources() + file));
    }

    public void write(Object object) {
        try {
            writer.write(object.toString() + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
