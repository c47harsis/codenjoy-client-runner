package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ClientServerServiceConfig {
    private String solutionFolderPath;
    private String solutionFolderPattern;
    private String codenjoyUrlRegex;
}
