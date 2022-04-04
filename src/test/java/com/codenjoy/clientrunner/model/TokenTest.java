package com.codenjoy.clientrunner.model;

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

import org.testng.annotations.Test;

import static com.codenjoy.clientrunner.ExceptionAssert.expectThrows;
import static org.testng.Assert.assertEquals;

public class TokenTest {

    private static final String SERVER_URL_PATTERN
            = "^https?://[0-9A-Za-z_.\\-:]+/codenjoy-contest/board/player/([\\w]+)\\?code=([\\w]+)";

    public static final String PLAYER_ID = "SuperMario";
    public static final String CODE = "000000000000";

    public static final String VALID_SERVER_URL
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
