package com.codenjoy.clientrunner;

import com.codenjoy.clientrunner.model.Token;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TokenTest {
    private static final String SERVER_URL_PATTERN
            = "^https?://[0-9A-Za-z_.\\-:]+/codenjoy-contest/board/player/([\\w]+)\\?code=([\\w]+)";

    private static final String PLAYER_ID = "SuperMario";
    private static final String CODE = "000000000000";

    private static final String VALID_SERVER_URL
            = "http://5.189.144.144/codenjoy-contest/board/player/" + PLAYER_ID + "?code=" + CODE;


    public static Token generateValidToken() {
        return Token.from(VALID_SERVER_URL, SERVER_URL_PATTERN);
    }

    @Test
    public void shouldGenerateValidToken_whenValidServerUrlPassed() {
        // when
        Token result = Token.from(VALID_SERVER_URL, SERVER_URL_PATTERN);

        // then
        assertEquals(result.getServerUrl(), VALID_SERVER_URL);
        assertEquals(result.getPlayerId(), PLAYER_ID);
        assertEquals(result.getCode(), CODE);
    }

    @Test
    public void shouldThrowException_whenInvalidServerUrlPassed() {
        try {
            Token.from("Invalid server URL", SERVER_URL_PATTERN);
            fail("Exception expected");
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(),
                    "Given invalid server URL: 'Invalid server URL' is not match " +
                            "'^https?://[0-9A-Za-z_.\\-:]+/codenjoy-contest/board/player/([\\w]+)\\?code=([\\w]+)'");
        }
    }

    @Test
    public void shouldThrowException_whenNullPassed_asServerUrl() {
        try {
            Token.from(null, "not null");
            fail("Exception expected");
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(),
                    "Server URL must not be null");
        }
    }

    @Test
    public void shouldThrowException_whenNullPassed_asUrlPattern() {
        try {
            Token.from("not null", null);
            fail("Exception expected");
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(),
                    "URL pattern must not be null");
        }
    }
}
