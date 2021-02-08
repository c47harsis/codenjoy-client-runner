package com.codenjoy.clientrunner.service;

import com.codenjoy.clientrunner.ClientRunnerApplication;
import com.codenjoy.clientrunner.config.ClientServerServiceConfig;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.model.Token;
import com.codenjoy.clientrunner.service.facade.DockerService;
import com.codenjoy.clientrunner.service.facade.GitService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.time.LocalDateTime;

import static com.codenjoy.clientrunner.model.Solution.Status.RUNNING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ClientRunnerApplication.class,
		properties = "spring.main.allow-bean-definition-overriding=true")
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class ClientServerServiceTest {

	public static final String SERVER_URL = "http://5.189.144.144/codenjoy-contest/board/player/0?code=000000000000";
	public static final String REPO = "https://github.com/codenjoyme/codenjoy-javascript-client.git";

	@MockBean
	private DockerService docker;

	@MockBean
	private GitService git;

	@Autowired
	private ClientServerServiceConfig config;

	@Autowired
	private SolutionManager solutionManager;

	@Autowired
	private ClientServerService service;

	@Before
	public void setup() {
		reset(docker, git);
	}

	@After
	public void cleanAll() {
		solutionManager.clear();
	}

	/**
	 * Stub for emulating FileSystem with java docker sources
 	 */
	private File javaSources() {
		return new File("./someFolder"){
			@Override
			public File[] listFiles() {
				return new File[]{ new File("pom.xml") };
			}

			@Override
			public boolean exists() {
				return true;
			}
		};
	}

	/**
	 * Emulate submitted solution that still in fiven status
	 * @param status
	 * @param containerId
	 * @return solution id
	 */
	private int givenValidSolution(Solution.Status status, String containerId) {
		Token token = Token.from(SERVER_URL, config.getServerRegex());
		Solution solution = Solution.from(token, javaSources());
		solution.setStatus(status);
		solution.setCreated(LocalDateTime.now());
		solution.setStarted(LocalDateTime.now());
		if (!status.isActive()) {
			solution.setFinished(LocalDateTime.now());
		}
		solution.setContainerId(containerId);
		solutionManager.add(solution);
		return solution.getId();
	}

	@Test
	public void shouldException_whenKillSolution_whichIsNotExists() {
		// given
		int nonExistsSolution = 234;

		// when
		try {
			service.killSolution(SERVER_URL, nonExistsSolution);
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
			// then
			assertEquals("For this token not found any solution with id '234'",
					e.getMessage());
		}
		verify(docker, never()).removeContainer(anyString());
	}

	@Test
	public void shouldRemoveContainer_whenKillSolution_whichIsExists_tokenIsOk() {
		// given
		int id = givenValidSolution(RUNNING, "containerId");

		// when
		service.killSolution(SERVER_URL, id);

		// then
		verify(docker, times(1)).killContainer("containerId");
	}

	@Test
	public void shouldException_whenKillSolution_whichIsExists_tokenIsNotOk() {
		// given
		int id = givenValidSolution(RUNNING, "containerId");

		// when
		try {
			service.killSolution(SERVER_URL + "badCode", id);
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
			// then
			assertEquals("For this token not found any solution with id '1'",
					e.getMessage());
		}
		verify(docker, never()).removeContainer(anyString());
	}

	@Test
	public void shouldException_whenKillSolution_andUrlIsBad() {
		// given
		int id = givenValidSolution(RUNNING, "containerId");

		// when
		try {
			service.killSolution("badUrl", id);
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
			// then
			assertEquals("Given invalid server URL: 'badUrl' is not match " +
							"'^https?://[0-9A-Za-z_.\\-:]+/codenjoy-contest/board/player/([\\w]+)\\?code=([\\w]+)'",
					e.getMessage());
		}
		verify(docker, never()).removeContainer(anyString());
	}

}
