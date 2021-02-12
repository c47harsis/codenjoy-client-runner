package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.ClientRunnerApplication;
import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.DockerService;
import com.codenjoy.clientrunner.service.facade.GitService;
import org.eclipse.jgit.api.Git;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertThrows;

@SpringBootTest(classes = ClientRunnerApplication.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class ClientRunnerServiceTest extends AbstractTestNGSpringContextTests {

    public static final String VALID_SERVER_URL = "http://5.189.144.144/codenjoy-contest/board/player/0?code=000000000000";
    public static final String VALID_REPO_URL = "https://github.com/codenjoyme/codenjoy-javascript-client.git";

    @InjectMocks
    @Autowired
    private ClientRunnerService service;

    @SpyBean
    private ClientServerServiceConfig config;

    @MockBean
    private DockerService docker;

    @MockBean
    private GitService git;

    @MockBean
    private SolutionManager solutionManager;

    @BeforeMethod
    public void setup() {
        reset(docker, git, solutionManager);
    }

    @Test
    public void shouldPullFromGitAndRunInSolutionManager_whenRunSolution_withValidCheckRequest() {
        // given
        doReturn(Optional.of(mock(Git.class)))
                .when(git)
                .clone(matches("\\.*.git"), isA(File.class));

        CheckRequest request = new CheckRequest();
        request.setServerUrl(VALID_SERVER_URL);
        request.setRepo(VALID_REPO_URL);

        // when
        service.checkSolution(request);

        // then
        verify(git, times(1)).clone(matches("\\.*.git"), isA(File.class));
        verify(solutionManager, times(1)).runSolution(isA(Token.class), isA(File.class));
    }

    @Test
    public void shouldThrowAnException_whenRunSolution_withRepoIsNotCloned() {
        // given
        doReturn(Optional.empty())
                .when(git)
                .clone(any(), any());

        CheckRequest request = new CheckRequest();
        request.setServerUrl(VALID_SERVER_URL);
        request.setRepo(VALID_REPO_URL);

        // when
        assertThrows(
                IllegalArgumentException.class,
                () -> service.checkSolution(request)
        );

        // then
        verify(git, only()).clone(any(), any());
        verify(solutionManager, never()).runSolution(any(), any());
    }
}
