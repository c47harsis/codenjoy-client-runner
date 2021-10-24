package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ClientServerServiceConfig {

    private Solutions solutions;
    private String serverRegex;

    @Data
    public static class Solutions {
        private String path;
        private String pattern;
    }
}
