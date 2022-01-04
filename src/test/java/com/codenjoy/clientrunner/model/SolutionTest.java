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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static com.codenjoy.clientrunner.ExceptionAssert.expectThrows;
import static com.codenjoy.clientrunner.model.Solution.Status.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class SolutionTest {

    private Token token = TokenTest.generateValidToken();
    private File javaSources;
    private Solution solution;

    @BeforeMethod
    public void setUp() {
        javaSources = new File("./");
        solution = Solution.from(token, javaSources);
    }

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
        assertEquals(solution.getSources(), javaSources);
        assertEquals(solution.getImageId(), null);
    }

    @Test
    public void shouldUpdateOnlyNotKilledStatus() {
        // given
        assertEquals(solution.getStatus(), NEW);

        // when then
        solution.setStatus(RUNNING);
        assertEquals(solution.getStatus(), RUNNING);

        // when then
        solution.setStatus(KILLED);
        assertEquals(solution.getStatus(), KILLED);

        // when then
        solution.setStatus(RUNNING);
        assertEquals(solution.getStatus(), KILLED); // still KILLED
    }

    @Test
    public void shouldFinalized_whenFinish_inNewStatus() {
        // given
        solution.setStatus(NEW);

        // when
        solution.finish();

        // then
        assertEquals(solution.getStatus(), ERROR);
        assertSetFinishedDate();
    }

    private void assertSetFinishedDate() {
        assertNotEquals(solution.getCreated(), null);
        assertEquals(solution.getStarted(), null);
        assertNotEquals(solution.getFinished(), null);
    }

    @Test
    public void shouldFinalized_whenFinish_inCompilingStatus() {
        // given
        solution.setStatus(COMPILING);

        // when
        solution.finish();

        // then
        assertEquals(solution.getStatus(), ERROR);
        assertSetFinishedDate();
    }

    @Test
    public void shouldFinalized_whenFinish_inRunningStatus() {
        // given
        solution.setStatus(RUNNING);

        // when
        solution.finish();

        // then
        assertEquals(solution.getStatus(), ERROR);
        assertSetFinishedDate();
    }

    @Test
    public void shouldFinalized_whenFinish_inKilledStatus() {
        // given
        solution.setStatus(KILLED);

        // when
        solution.finish();

        // then
        assertEquals(solution.getStatus(), FINISHED);
        assertSetFinishedDate();
    }

    @Test
    public void shouldFinalized_whenFinish_inErrorStatus() {
        // given
        solution.setStatus(ERROR);

        // when
        solution.finish();

        // then
        assertEquals(solution.getStatus(), ERROR);
        assertSetFinishedDate();
    }

    @Test
    public void shouldFinalized_whenFinish_inFinishedStatus() {
        // given
        solution.setStatus(FINISHED);

        // when
        solution.finish();

        // then
        assertEquals(solution.getStatus(), FINISHED);
        assertSetFinishedDate();
    }


}
