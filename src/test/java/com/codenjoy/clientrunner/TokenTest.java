package com.codenjoy.clientrunner;

import com.codenjoy.clientrunner.model.Token;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TokenTest {
    private static final String SERVER_URL_PATTERN
            = "^https?://[0-9A-Za-z_.\\-:]+/codenjoy-contest/board/player/([\\w]+)\\?code=([\\w]+)";

    private static final String PLAYER_ID = "SuperMario";
    private static final String CODE = "000000000000";

    private static final String VALID_SERvER_URL
            = "http://5.189.144.144/codenjoy-contest/board/player/" + PLAYER_ID + "?code=" + CODE;


    public static Token generateValidToken() {
        return Token.from(VALID_SERvER_URL, SERVER_URL_PATTERN);
    }

    @Test
    public void shouldGenerateValidToken_whenValidServerUrlPassed() {
        // when
        Token result = Token.from(VALID_SERvER_URL, SERVER_URL_PATTERN);

        // then
        assertEquals(VALID_SERvER_URL, result.getServerUrl());
        assertEquals(PLAYER_ID, result.getPlayerId());
        assertEquals(CODE, result.getCode());
    }

    @Test
    public void shouldThrowAnException_whenInvalidServerUrlPassed() {
        String invalidServerUrl = "I am invalid server URL";

        assertThrows(
                IllegalArgumentException.class,
                () -> Token.from(invalidServerUrl, SERVER_URL_PATTERN)
        );
    }

    @Test
    public void shouldThrowIllegalArgumentException_whenNullPassed() {
        try {
            Token.from("not null", null);
            Token.from(null, "not null");
            Token.from(null, null);
            fail("IllegalArgumentException was expected");
        } catch (Throwable ex) {
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }
}
