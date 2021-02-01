package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ClientServerServiceConfig {
    private String solutionFolderPath = "./solutions";
    private String solutionFolderPattern = "yyyy-MM-dd'_'HH-mm-ss";
    private String codenjoyUrlRegex;

    // Docker's defaults
    // TODO: container is killed when memory limit exceeded. Need to check this behaviour
    private Integer containerMemoryLimitMB = 0;
    private Long containerCpuPeriod = 100000L;
    private Long containerCpuQuota = -1L;

    public void setContainerMemoryLimitMB(Integer value) {
        if (value < 8) {
            value = 8;
        }
        containerMemoryLimitMB = value;
    }
}
