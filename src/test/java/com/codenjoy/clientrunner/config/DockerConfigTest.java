package com.codenjoy.clientrunner.config;

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