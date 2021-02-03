package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.ValidationException;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ClientServerServiceConfig {

    private SolutionFolder solutionFolder;
    private String codenjoyUrlRegex;

    @Data
    public static class SolutionFolder {
        private String path;
        private String pattern;
    }
}
