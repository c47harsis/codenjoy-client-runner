package com.codenjoy.clientrunner;

import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.service.ClientServerService;
import com.codenjoy.clientrunner.service.SolutionManager;
import com.codenjoy.clientrunner.service.facade.DockerService;
import com.codenjoy.clientrunner.service.facade.GitService;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.codenjoy.clientrunner.model.Solution.Status.*;
import static junit.framework.TestCase.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

@SpringBootTest(classes = ClientRunnerApplication.class,
		properties = "spring.main.allow-bean-definition-overriding=true")
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class SmokeTest {

	@Autowired
	private WebApplicationContext context;

	@SpyBean
	private DockerService docker;

	@SpyBean
	private GitService git;

	@SpyBean
	private SolutionManager solutionManager;

	@Autowired
	private ClientServerService service;
	
	private List<SolutionSummary> solutions;
	private List<String> logs;

	@SneakyThrows
	@Test
	public void shouldCheckTwoSolutions_forSameServerUrlAndRepo() {
		String serverUrl = "http://5.189.144.144/codenjoy-contest/board/player/0?code=000000000000";
		String repo = "https://github.com/codenjoyme/codenjoy-javascript-client.git";

		// given empty solutions at start
		refreshSolutions(serverUrl);
		assertEquals(0, solutions.size());

		// when try to check one solution
		createSolution(serverUrl, repo);
		waitForBuildSolution(serverUrl, 1);

		// then new solution in RUNNING mode
		assertThat(solution(0)).hasId(1).inStatus(RUNNING);

		// when run another solution for same player/code
		createSolution(serverUrl, repo);
		waitForBuildSolution(serverUrl, 2);

		// then one was KILLED
		assertThat(solution(0)).hasId(1).inStatus(KILLED);
		// another one is still RUNNING
		assertThat(solution(1)).hasId(2).inStatus(RUNNING);

		// when get RUNNING solution
		SolutionSummary solution = service.getSolutionSummary(serverUrl, 2);
		assertThat(solution).hasId(2).inStatus(RUNNING);

		// when get KILLED solution
		solution = service.getSolutionSummary(serverUrl, 1);
		assertThat(solution).hasId(1).inStatus(KILLED);

		// when get build logs of RUNNING solution
		readBuildLogs(serverUrl, 2);
		assertLastLineStartsWith("Successfully built");

		// when get running logs of RUNNING solution
		readRuntimeLogs(serverUrl, 2);
		assertFistLineStartsWith("STDOUT: Got url from Environment: " + serverUrl);

		// when get build logs of KILLED solution
		readBuildLogs(serverUrl, 1);
		assertLastLineStartsWith("Successfully built");

		// when get running logs of KILLED solution
		readRuntimeLogs(serverUrl, 1);
		assertFistLineStartsWith("STDOUT: Got url from Environment: " + serverUrl);

		// when kill RUNNING solution
		service.killSolution(serverUrl, 2);
		refreshSolutions(serverUrl);

		// then one was KILLED
		assertThat(solution(0)).hasId(1).inStatus(KILLED);
		// another one is also KILLED
		assertThat(solution(1)).hasId(2).inStatus(KILLED);

	}

	@SneakyThrows
	private void readRuntimeLogs(String serverUrl, int solutionId) {
		do {
			logs = service.getRuntimeLogs(serverUrl, solutionId, 0);
			Thread.sleep(1000);
		} while (logs.isEmpty());
	}

	@SneakyThrows
	private void readBuildLogs(String serverUrl, int solutionId) {
		do {
			logs = service.getBuildLogs(serverUrl, solutionId, 0);
			Thread.sleep(1000);
		} while (logs.isEmpty());
	}

	private void assertFistLineStartsWith(String prefix) {
		assertTrue(logs.get(0).startsWith(prefix));
	}

	private void assertLastLineStartsWith(String prefix) {
		assertTrue(logs.get(logs.size() - 1).startsWith(prefix));
	}

	public class AssertSolution {

		private SolutionSummary solution;

		public AssertSolution(SolutionSummary solution) {
			this.solution = solution;
		}

		public AssertSolution hasId(int id) {
			assertEquals(id, solution.getId());
			return this;
		}

		public AssertSolution inStatus(Solution.Status status) {
			assertEquals(status.name(), solution.getStatus());

			switch (Solution.Status.valueOf(solution.getStatus())) {
				case NEW :
				case COMPILING :
					assertNotSame(null, solution.getCreated());
					assertSame(null, solution.getStarted());
					assertSame(null, solution.getFinished());
					break;
				case RUNNING :
					assertNotSame(null, solution.getCreated());
					assertNotSame(null, solution.getStarted());
					assertSame(null, solution.getFinished());
					break;
				case FINISHED :
				case ERROR :
				case KILLED :
					assertNotSame(null, solution.getCreated());
					assertNotSame(null, solution.getStarted());
					assertNotSame(null, solution.getFinished());
					break;

			}
			return this;
		}
	}

	private AssertSolution assertThat(SolutionSummary solution) {
		return new AssertSolution(solution);
	}

	private void refreshSolutions(String serverUrl) {
		solutions = service.getAllSolutionsSummary(serverUrl);
	}

	private SolutionSummary solution(int index) {
		return solutions.get(index);
	}

	private void createSolution(String serverUrl, String repo) {
		service.checkSolution(new CheckRequest(){{
			setRepo(repo);
			setServerUrl(serverUrl);
		}});
	}

	private void waitForBuildSolution(String serverUrl, int total) throws InterruptedException {
		do {
			Thread.sleep(1000);
			refreshSolutions(serverUrl);
			if (solutions.size() != total) continue;
		} while (!RUNNING.name().equals(last().getStatus()));
	}

	private SolutionSummary last() {
		return solutions.get(solutions.size() - 1);
	}
}
