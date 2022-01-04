package com.codenjoy.clientrunner.config;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2012 - 2022 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

        public void setMemoryLimitMB(int value) {
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
