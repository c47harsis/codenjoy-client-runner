package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ClientServerServiceConfig {
    private String solutionsFolderPath = "./solutions";
    private String codenjoyUrlRegex = "^https://dojorena.io/codenjoy-contest/board/player/([\\w]+)\\?code=([\\w]+)";
}
