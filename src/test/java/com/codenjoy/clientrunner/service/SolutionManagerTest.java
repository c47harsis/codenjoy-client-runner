package com.codenjoy.clientrunner.service;

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

import com.codenjoy.clientrunner.model.*;
import com.codenjoy.clientrunner.config.DockerConfig;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.exception.SolutionNotFoundException;
import com.codenjoy.clientrunner.service.facade.DockerService;
import com.codenjoy.clientrunner.service.facade.LogWriter;
import lombok.SneakyThrows;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static com.codenjoy.clientrunner.ExceptionAssert.expectThrows;
import static com.codenjoy.clientrunner.model.Solution.Status.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@SpringBootTest
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class SolutionManagerTest extends AbstractTestNGSpringContextTests {

    @MockBean
    private DockerService dockerService;

    @SpyBean
    private DockerConfig config;

    @Autowired
    private SolutionManager solutionManager;

    private File sources;
    private Token token;
    private int id;

    @BeforeMethod
    @SneakyThrows
    public void generateJavaSources() {
        Path path = Path.of("./target/testJavaSources-" +
                new Random().nextInt(Integer.MAX_VALUE));
        path = Files.createDirectory(path);
        Files.createFile(path.resolve("pom.xml"));
        Files.createFile(path.resolve(Platform.JAVA.getFilename()));
        sources = path.toFile();
    }

    @BeforeMethod
    public void setup() {
        reset(config, dockerService);
        solutionManager.clear();
        token = TokenTest.generateValidToken();
    }

    @AfterMethod
    public void cleanup() {
        solutionManager.killAll(token);
        reset(dockerService);
    }

    @Test
    public void shouldAddDockerfile_whenRunSolution_withValidTokenAndSources() {
        // when
        solutionManager.runSolution(token, sources);

        // then
        assertEquals(Files.exists(Path.of(sources.getPath(), "Dockerfile")), true);
    }

    @Test
    public void shouldRunContainer_afterBuildImage() {
        // given
        String imageId = "imageId";
        String containerId = "containerId";

        whenImageBuilt_thenRunContainer(imageId);
        when(dockerService.createContainer(anyString(), any())).thenReturn(containerId);
        whenWaitRunContainer_thenWriteLogs();

        // when
        id = solutionManager.runSolution(token, sources);

        // then
        InOrder inOrder = inOrder(dockerService);
        inOrder.verify(dockerService).createContainer(same(imageId), any());
        inOrder.verify(dockerService).startContainer(same(containerId));
        inOrder.verify(dockerService).logContainer(same(containerId), any());
        inOrder.verify(dockerService).waitContainer(same(containerId), any());
        inOrder.verifyNoMoreInteractions();

        SolutionSummary solution = solutionManager.getSolutionSummary(token, id);
        assertEquals(solution.getId(), id);
        assertEquals(solution.getStatus(), RUNNING.name());
        assertNotEquals(solution.getCreated(), null);
        assertNotEquals(solution.getStarted(), null);
        assertEquals(solution.getFinished(), null);
    }

    @Test
    public void shouldGetBuildLogs_afterBuildImage() {
        // given
        shouldRunContainer_afterBuildImage();

        // when
        List<String> logs = solutionManager.getLogs(token, id, LogType.BUILD, 0);

        // then
        assertEquals(logs.toString(), "[Building started, Building finished]");
    }

    @Test
    public void shouldGetRuntimeLogs_afterBuildImage() {
        // given
        shouldRunContainer_afterBuildImage();

        // when
        List<String> logs = solutionManager.getLogs(token, id, LogType.RUNTIME, 0);

        // then
        assertEquals(logs.toString(), "[Running started, Running finished]");
    }

    @Test
    public void shouldGetBuildLogs_afterKillSolution() {
        // given
        shouldWaitStopContainer_afterRunContainer();

        // when
        List<String> logs = solutionManager.getLogs(token, id, LogType.BUILD, 0);

        // then
        assertEquals(logs.toString(), "[Building started, Building finished]");
    }

    @Test
    public void shouldGetRuntimeLogs_afterKillSolution() {
        // given
        shouldWaitStopContainer_afterRunContainer();

        // when
        List<String> logs = solutionManager.getLogs(token, id, LogType.RUNTIME, 0);

        // then
        assertEquals(logs.toString(), "[Running started, Running finished]");
    }

    @Test
    public void shouldWaitStopContainer_afterRunContainer() {
        // given
        String imageId = "imageId";
        String containerId = "containerId";

        whenImageBuilt_thenRunContainer(imageId);
        when(dockerService.createContainer(anyString(), any())).thenReturn(containerId);
        whenWaitRunContainer_thenWriteLogs();
        whenWaitContainer_thenFinishedSolution();

        // when
        id = solutionManager.runSolution(token, sources);

        // then
        InOrder inOrder = inOrder(dockerService);
        inOrder.verify(dockerService).createContainer(same(imageId), any());
        inOrder.verify(dockerService).startContainer(same(containerId));
        inOrder.verify(dockerService).logContainer(same(containerId), any());
        inOrder.verify(dockerService).waitContainer(same(containerId), any());
        inOrder.verify(dockerService).removeContainer(same(containerId));
        inOrder.verifyNoMoreInteractions();

        SolutionSummary solution = solutionManager.getSolutionSummary(token, id);
        assertEquals(solution.getId(), id);
        assertEquals(solution.getStatus(), ERROR.name());
        assertNotEquals(solution.getCreated(), null);
        assertNotEquals(solution.getStarted(), null);
        assertNotEquals(solution.getFinished(), null);
    }

    private void whenWaitContainer_thenFinishedSolution() {
        doAnswer(invocation -> {
            invocation.getArgument(1, Runnable.class).run();
            return null;
        }).when(dockerService).waitContainer(any(), any());
    }

    private void whenWaitRunContainer_thenWriteLogs() {
        doAnswer(invocation -> {
            writeLogs(invocation, "Running", 1, "\n");
            return null;
        }).when(dockerService).logContainer(any(), any());
    }

    private void whenImageBuilt_thenRunContainer(String imageId) {
        doAnswer(invocation -> {
            writeLogs(invocation, "Building", 3, "\n");
            invocation.getArgument(4, Consumer.class).accept(imageId);
            return null;
        }).when(dockerService).buildImage(any(),
                anyString(), anyString(),
                any(), any());
    }

    private void writeLogs(InvocationOnMock invocation, String phase, int index, String newLine) {
        LogWriter writer = invocation.getArgument(index, LogWriter.class);
        writer.write(phase + " started" + newLine);
        writer.write(phase + " finished" + newLine);
        writer.close();
    }

    @Test
    public void shouldCallDockerBuildImage_whenRunSolution_withValidTokenAndSources() {
        // when
        solutionManager.runSolution(token, sources);

        // then
        verify(dockerService, only()).buildImage(same(sources),
                same(token.getGameToRun()), same(token.getServerUrl()),
                any(), any());
    }

    @Test
    @SneakyThrows
    public void shouldSetErrorSolutionStatus_whenRunSolution_withDockerBuildImageThrowAnException() {
        // given
        doThrow(RuntimeException.class)
                .when(dockerService)
                .buildImage(any(),
                        anyString(), anyString(),
                        any(), any());

        // when
        id = solutionManager.runSolution(token, sources);

        // then
        assertEquals(statusOf(id), ERROR);
    }

    @Test
    public void shouldKillLastSolutionBeforeRunNew_whenRunSolution_withValidTokenAndSources() {
        // given
        int id1 = solutionManager.runSolution(token, sources);
        int id2 = solutionManager.runSolution(token, sources);

        // when
        int id3 = solutionManager.runSolution(token, sources);

        // then
        assertEquals(statusOf(id1).isActive(), false);
        assertEquals(statusOf(id2).isActive(), false);
        assertEquals(statusOf(id3).isActive(), true);
    }

    @Test
   public void shouldDontRunSolution_whenKillItImmediately_afterRunIt() {
        // given
        // simulate multithreading TODO to use synchronized section inside solutionManager
        when(config.getDockerfilesFolder()).thenAnswer(invocation -> {
            kill(token);
            return invocation.callRealMethod();
        });

        // when
        solutionManager.runSolution(token, sources);

        // then
        SolutionSummary summary = solutionManager.getAllSolutionSummary(token).get(0);
        assertEquals(summary.getStatus(), KILLED.name());
        verify(dockerService, never()).buildImage(any(),
                anyString(), anyString(),
                any(), any());
    }

    private void kill(Token token) {
        SolutionSummary summary = solutionManager.getAllSolutionSummary(token).get(0);
        solutionManager.kill(token, summary.getId());
    }

    @Test
    public void shouldKillTheSolution_whenKill_withExistingSolution() {
        // given
        id = solutionManager.runSolution(token, sources);

        // when
        solutionManager.kill(token, id);

        // then
        assertEquals(statusOf(id).isActive(), false);
    }

    @Test
    public void shouldThrowAnException_whenKill_withNonExistingSolution() {
        // given
        id = solutionManager.runSolution(token, sources);

        // then
        expectThrows(SolutionNotFoundException.class,
                "Solution with id:-1 not found!",
                // when
                () -> solutionManager.kill(token, -1));

        assertEquals(statusOf(id).isActive(), true);
    }

    private Solution.Status statusOf(int solutionId) {
        SolutionSummary solution = solutionManager.getSolutionSummary(token, solutionId);
        return Solution.Status.valueOf(solution.getStatus());
    }
}
