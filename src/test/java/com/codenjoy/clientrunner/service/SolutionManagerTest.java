package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.TokenTest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.DockerService;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.codenjoy.clientrunner.model.Solution.Status.ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@SpringBootTest
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class SolutionManagerTest extends AbstractTestNGSpringContextTests {

    @MockBean
    private DockerService dockerService;

    @Autowired
    private SolutionManager solutionManager;

    private File sources;
    private Token token;

    @BeforeMethod
    @SneakyThrows
    public void generateJavaSources() {
        Path sourcesPath = Path.of("./testJavaSources");
        FileUtils.deleteDirectory(sourcesPath.toFile());
        Path sources = Files.createDirectory(sourcesPath);
        Files.createFile(sources.resolve("pom.xml"));
        this.sources = sources.toFile();
    }

    @BeforeMethod
    public void generateValidToken() {
        this.token = TokenTest.generateValidToken();
    }

    @SneakyThrows
    @AfterMethod
    public void cleanup() {
        FileUtils.deleteDirectory(sources);
        reset(dockerService);
    }

    @Test
    public void shouldAddDockerfile_whenRunSolution_withValidTokenAndSources() {
        // when
        solutionManager.runSolution(token, sources);

        // then
        assertTrue(Files.exists(Path.of(sources.getPath(), "Dockerfile")));
    }

    @Test
    public void shouldCallDockerBuildImage_whenRunSolution_withValidTokenAndSources() {
        // when
        solutionManager.runSolution(token, sources);

        // then
        verify(dockerService, only()).buildImage(same(sources), same(token.getServerUrl()), any(), any());
    }

    @Test
    @SneakyThrows
    public void shouldSetErrorSolutionStatus_whenRunSolution_withDockerBuildImageThrowAnException() {
        // given
        doThrow(RuntimeException.class)
                .when(dockerService)
                .buildImage(isA(File.class), anyString(), any(), any());

        // when
        solutionManager.runSolution(token, sources);

        // then
        assertEquals(ERROR.name(), solutionManager.getSolutionSummary(token, 0).getStatus());
    }

    @Test
    public void shouldKillLastSolutionBeforeRunNew_whenRunSolution_withValidTokenAndSources() {
        // given
        solutionManager.runSolution(token, sources);
        solutionManager.runSolution(token, sources);

        // when
        solutionManager.runSolution(token, sources);

        // then
        assertFalse(statusOfSolutionById(1).isActive());
        assertFalse(statusOfSolutionById(2).isActive());
        assertTrue(statusOfSolutionById(3).isActive());
    }

    private Solution.Status statusOfSolutionById(int solutionId) {
        SolutionSummary solution = solutionManager.getSolutionSummary(token, solutionId);
        return Solution.Status.valueOf(solution.getStatus());
    }
}
