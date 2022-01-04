package com.codenjoy.clientrunner.model;

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

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static com.codenjoy.clientrunner.service.SolutionManager.DOCKERFILE;
import static org.testng.Assert.*;

public class PlatformTest {

    @Test
    public void shouldGetPlatform_byFileName() {
        Arrays.stream(Platform.values())
                .forEach(this::assertPlatformFilename);
    }

    @Test
    public void shouldPlatformDockerfileExists() {
        Arrays.stream(Platform.values())
                .forEach(this::assertPlatformExists);
    }

    private void assertPlatformExists(Platform platform) {
        String folder = platform.getFolderName();
        assertEquals(platform.getFolderName(), folder);
        String path = "/dockerfiles/" + folder + "/" + DOCKERFILE;
        URL url = getClass().getResource(path);
        assertEquals(url != null && new File(url.getPath()).exists(), true,
                "Expected Dockerfile in: '" + path + "'");
    }

    private void assertPlatformFilename(Platform platform) {
        assertEquals(Platform.of(platform.getFilename()), platform);
    }

}
