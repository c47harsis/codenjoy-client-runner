package com.codenjoy.clientrunner;

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
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
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

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ClientRunnerApplication.class,
		properties = "spring.main.allow-bean-definition-overriding=true")
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class SmokeTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private WebApplicationContext context;

	@SpyBean
	private DockerService docker;

	@SpyBean
	private GitService git;

	@SpyBean
	private SolutionManager solutions;

	private MockMvc mvc;

	@Before
	public void setup() {
		if (mvc == null) {
			mvc = MockMvcBuilders.webAppContextSetup(context).build();
		}
	}

	@SneakyThrows
	protected String mapToJson(Object obj) {
		return new ObjectMapper().writeValueAsString(obj);
	}

	@SneakyThrows
	protected <T> T mapFromJson(String json, Class<T> clazz) {
		return new ObjectMapper().readValue(json, clazz);
	}

	@SneakyThrows
	private String get(String uri) {
		return process(200, MockMvcRequestBuilders.get(uri));
	}

	@SneakyThrows
	private String get(int status, String uri) {
		return process(status, MockMvcRequestBuilders.get(uri));
	}

	@SneakyThrows
	private String post(int status, String uri) {
		return process(status, MockMvcRequestBuilders.post(uri));
	}

	@SneakyThrows
	private String post(String uri) {
		return process(200, MockMvcRequestBuilders.post(uri));
	}

	@SneakyThrows
	private String post(int status, String uri, String data) {
		return process(status, MockMvcRequestBuilders.post(uri, data)
				.contentType(MediaType.APPLICATION_JSON)
				.content(data));
	}

	private String process(int status, MockHttpServletRequestBuilder post) throws Exception {
		MvcResult mvcResult = mvc.perform(post
				.accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		assertEquals(result, status, mvcResult.getResponse().getStatus());
		return result;
	}

	private void assertPost(String url, String json, String expected) {
		assertPost(200, url, json, expected);
	}

	private void assertPost(int status, String url, String json, String expected) {
		String result = post(status, url, fix(json));
		assertJson(expected, result);
	}

	private void assertPostError(int status, String url, String json, String expectedMessage) {
		String result = post(status, url, fix(json));
		assertError(expectedMessage, result);
	}

	@SneakyThrows
	private void assertError(String message, String source) {
		JSONObject error = tryParseAsJson(source);
		assertEquals(message, error.getString("message"));
	}

	@SneakyThrows
	private JSONObject tryParseAsJson(String source) {
		try {
			return new JSONObject(source);
		} catch (JSONException e) {
			return new JSONObject(){{
				put("message", source);
			}};
		}
	}

	private void assertGet(String url, String expected) {
		String result = get(url);
		assertJson(expected, result);
	}

	private void assertJson(String expected, String actual) {
		boolean isJson = actual.startsWith("{");
		assertEquals(expected,
				isJson ? fix2(prettyPrint(actual)) : actual);
	}

	public static String prettyPrint(Object object) {
		String json = toStringSorted(object);
		return clean(json);
	}

	@SneakyThrows
	public static String toStringSorted(Object object) {
		int indentSpaces = 4;
		if (object instanceof Collection) {
			return new JSONArray((Collection) object).toString(indentSpaces);
		} else if (object instanceof JSONArray) {
			return ((JSONArray)object).toString(indentSpaces);
		} else if (object instanceof JSONObject) {
			return new JSONObject(object.toString()).toString(indentSpaces);
		} else {
			return new JSONObject(object.toString()).toString(indentSpaces);
		}
	}

	public static String clean(String json) {
		return json.replace('\"', '\'').replaceAll("\\r\\n", "\n");
	}

	private String fix(String json) {
		return json.replace("'", "\"");
	}

	private String fix2(String json) {
		return json.replace("\"", "'");
	}

	@SneakyThrows
	@Test
	public void simpleTest() {
		String serverUrl = "http://5.189.144.144/codenjoy-contest/board/player/0?code=000000000000";

		mvc.perform(MockMvcRequestBuilders.get("/solutions/all"))
					.andExpect(status().isOk());

//		assertGet("/solutions/all?serverUrl=" + encode(serverUrl),
//
//				"{}");
	}

	@SneakyThrows
	private String encode(String string) {
		return URLEncoder.encode(string, StandardCharsets.UTF_8.name());
	}

}
