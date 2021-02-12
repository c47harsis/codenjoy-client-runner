package com.codenjoy.clientrunner.model;

import org.testng.annotations.Test;

import java.io.File;

import static com.codenjoy.clientrunner.ExceptionAssert.expectThrows;
import static com.codenjoy.clientrunner.model.Solution.Status.NEW;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class SolutionTest {

    private Token token = TokenTest.generateValidToken();

    @Test
    public void shouldThrowException_whenSourcesNotFound() {
        // then
        expectThrows(IllegalArgumentException.class,
                "Source folder with path 'bad-path' doesn't exist",
                // when
                () -> Solution.from(token, new File("bad-path")));
    }

    @Test
    public void shouldThrowException_whenPlatformIsInvalid() {
        // then
        expectThrows(IllegalArgumentException.class,
                "Solution platform not supported for sources: '.\\target'",
                // when
                () -> Solution.from(token, new File("./target")));
    }

    @Test
    public void shouldBuildSolution_whenEverythingIsOk() {
        // given
        File sources = new File("./");

        // when
        Solution solution = Solution.from(token, sources);

        // then
        assertEquals(solution.getId(), 0);
        assertEquals(solution.getStatus(), NEW);
        assertEquals(solution.getPlatform(), Platform.JAVA);

        assertEquals(solution.getServerUrl(), TokenTest.VALID_SERVER_URL);
        assertEquals(solution.getCode(), TokenTest.CODE);
        assertEquals(solution.getPlayerId(), TokenTest.PLAYER_ID);

        assertNotEquals(solution.getCreated(), null);
        assertEquals(solution.getStarted(), null);
        assertEquals(solution.getFinished(), null);

        assertEquals(solution.getContainerId(), null);
        assertEquals(solution.getSources(), sources);
        assertEquals(solution.getImageId(), null);
    }

}
