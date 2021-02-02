package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.ValidationException;

@Data
@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerConfig {

    private Container container;
    private String dockerfilesFolder;

    @Data
    public static class Container {
        private static final long BYTES_IN_MB = 1024 * 1024;

        // TODO: container gets killed when memory limit exceeded. Need to check this behaviour
        private Integer memoryLimitMB;
        private Long cpuPeriod;
        private Long cpuQuota;

        public void setMemoryLimitMB(Integer value) {
            if (value >= 6 || value == 0) {
                memoryLimitMB = value;
            } else {
                throw new ValidationException("Set 0 for docker's default container memory limit," +
                        " otherwise the value should be not less than 6MB");
            }
        }

        public Long getMemoryLimitBytes() {
            return BYTES_IN_MB * memoryLimitMB;
        }
    }
}
