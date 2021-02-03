package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ClientServerServiceConfig {

    private SolutionFolder solutionFolder;
    private String serverRegex;

    public DateTimeFormatter getSolutionFolderFormatter() {
        return DateTimeFormatter.ofPattern(getSolutionFolder().getPattern());
    }

    @Data
    public static class SolutionFolder {
        private String path;
        private String pattern;
    }
}
