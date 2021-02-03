package com.codenjoy.clientrunner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.ValidationException;

@Data
@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerConfig {

    public static final int MINIMAL_MEMORY_LIMIT = 6;
    private Container container;
    private String dockerfilesFolder;

    @Data
    public static class Container {
        private static final long BYTES_IN_MB = 1024L * 1024L;

        // TODO: container gets killed when memory limit exceeded. Need to check this behaviour
        private int memoryLimitMB;
        private long cpuPeriod;
        private long cpuQuota;

        public void setMemoryLimitMB(Integer value) {
            if (value != 0 && value < MINIMAL_MEMORY_LIMIT) {
                throw new ValidationException("Set 0 for docker's default container memory limit," +
                        " otherwise the value should be not less than 6MB");
            }

            memoryLimitMB = value;
        }

        public long getMemoryLimitBytes() {
            return BYTES_IN_MB * memoryLimitMB;
        }
    }
}
