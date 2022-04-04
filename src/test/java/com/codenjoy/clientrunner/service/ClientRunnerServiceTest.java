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
