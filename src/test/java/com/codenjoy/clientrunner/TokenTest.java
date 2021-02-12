package com.codenjoy.clientrunner;

import com.codenjoy.clientrunner.model.Token;
import org.testng.annotations.Test;

import static com.codenjoy.clientrunner.ExceptionAssert.expectThrows;
import static org.testng.Assert.assertEquals;

public class TokenTest {
    private static final String SERVER_URL_PATTERN
            = "^https?://[0-9A-Za-z_.\\-:]+/codenjoy-contest/board/player/([\\w]+)\\?code=([\\w]+)";

    private static final String PLAYER_ID = "SuperMario";
    private static final String CODE = "000000000000";

    private static final String VALID_SERVER_URL
            = "http://5.189.144.144/codenjoy-contest/board/player/" +
            PLAYER_ID + "?code=" + CODE;


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
        expectThrows(IllegalArgumentException.class,
                "Given invalid server URL: 'Invalid server URL' is not match",
            () -> Token.from("Invalid server URL", SERVER_URL_PATTERN));
    }

    @Test
    public void shouldThrowException_whenNullPassed_asServerUrl() {
        expectThrows(IllegalArgumentException.class,
                "Server URL must not be null",
                () -> Token.from(null, "not null"));
    }

    @Test
    public void shouldThrowException_whenNullPassed_asUrlPattern() {
        expectThrows(IllegalArgumentException.class,
                "URL pattern must not be null",
                () -> Token.from("not null", null));
    }
}
