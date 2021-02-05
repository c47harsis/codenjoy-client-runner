package com.codenjoy.clientrunner;

import com.codenjoy.clientrunner.dto.CheckRequest;
import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.service.ClientServerService;
import com.codenjoy.clientrunner.service.SolutionManager;
import com.codenjoy.clientrunner.service.facade.DockerService;
import com.codenjoy.clientrunner.service.facade.GitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static com.codenjoy.clientrunner.model.Solution.Status.KILLED;
import static com.codenjoy.clientrunner.model.Solution.Status.RUNNING;
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
	private SolutionManager solutions;

	@Autowired
	private ClientServerService service;

	@SneakyThrows
	@Test
	public void simpleTest() {
		String serverUrl = "http://5.189.144.144/codenjoy-contest/board/player/0?code=000000000000";
		String repo = "https://github.com/codenjoyme/codenjoy-javascript-client.git";

		// given
		// empty solutions at start
		List<SolutionSummary> solutions = service.getAllSolutionsSummary(serverUrl);
		assertEquals(0, solutions.size());

		// when
		// try to check one solution
		service.checkSolution(new CheckRequest(){{
			setRepo(repo);
			setServerUrl(serverUrl);
		}});

		// when
		// wait for building
		solutions = waitForNewSolution(serverUrl, 1);
		SolutionSummary solution1 = solutions.get(0);

		// then
		assertEquals(1, solution1.getId());
		assertNotSame(null, solution1.getCreated());
		assertNotSame(null, solution1.getStarted());
		assertEquals(null, solution1.getFinished());
		assertEquals(RUNNING.name(), solution1.getStatus());

		// when
		// run another solution for same player/code
		service.checkSolution(new CheckRequest(){{
			setRepo(repo);
			setServerUrl(serverUrl);
		}});

		// when
		// wait for building
		solutions = waitForNewSolution(serverUrl, 2);
		solution1 = solutions.get(0);
		SolutionSummary solution2 = solutions.get(1);


		// one was KILLED
		assertEquals(1, solution1.getId());
		assertNotSame(null, solution1.getCreated());
		assertNotSame(null, solution1.getStarted());
		assertNotSame(null, solution1.getFinished());
		assertEquals(KILLED.name(), solution1.getStatus());

		// another one is still RUNNING
		assertEquals(2, solution2.getId());
		assertNotSame(null, solution2.getCreated());
		assertNotSame(null, solution2.getStarted());
		assertEquals(null, solution2.getFinished());
		assertEquals(RUNNING.name(), solution2.getStatus());

	}

	private List<SolutionSummary> waitForNewSolution(String serverUrl, int total) throws InterruptedException {
		List<SolutionSummary> solutions;
		SolutionSummary solution = null;
		do {
			Thread.sleep(1000);
			solutions = service.getAllSolutionsSummary(serverUrl);

			if (solutions.size() != total) continue;

			// then
			// one solution exists
			assertEquals(total, solutions.size());
			solution = solutions.get(solutions.size() - 1);
		} while (!RUNNING.name().equals(solution.getStatus()));
		return solutions;
	}
}
