package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ClientServerServiceConfig {

    private SolutionFolder solutionFolder;
    private String serverRegex;

    @Data
    public static class SolutionFolder {
        private String path;
        private String pattern;
    }
}
