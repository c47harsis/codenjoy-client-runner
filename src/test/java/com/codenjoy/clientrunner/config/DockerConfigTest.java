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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.ValidationException;

import static com.codenjoy.clientrunner.ExceptionAssert.expectThrows;
import static org.testng.Assert.assertEquals;

public class DockerConfigTest {

    private DockerConfig.Container container;

    @BeforeMethod
    public void setUp() {
        container = new DockerConfig.Container();
    }

    @Test
    public void shouldValidateException_whenBadMemoryLimit() {
        // then
        expectThrows(ValidationException.class,
                "the value should be not less than 6MB",
                // when
                () -> container.setMemoryLimitMB(1));
    }

    @Test
    public void shouldSet_whenMemoryLimitIsZerro() {
        // when
        container.setMemoryLimitMB(0);

        // then
        assertEquals(container.getMemoryLimitMB(), 0);
    }

    @Test
    public void shouldSet_whenMemoryLimitIsMoreThanMinimal() {
        // when
        int valid = DockerConfig.MINIMAL_MEMORY_LIMIT + 1;
        container.setMemoryLimitMB(valid);

        // then
        assertEquals(container.getMemoryLimitMB(), valid);
    }

}
