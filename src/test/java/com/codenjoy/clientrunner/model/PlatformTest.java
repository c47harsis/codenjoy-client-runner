package com.codenjoy.clientrunner.model;

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