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

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Token {

    private final String gameToRun;
    private final String serverUrl;
    private final String playerId;
    private final String code;

    public static Token from(String serverUrl, String urlPattern) {
        checkArgument(serverUrl != null, "Server URL must not be null");
        checkArgument(urlPattern != null, "URL pattern must not be null");
        Pattern serverUrlPattern = Pattern.compile(urlPattern);
        Matcher matcher = serverUrlPattern.matcher(serverUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Given invalid server URL: '%s' " +
                            "is not match '%s'", serverUrl, urlPattern));
        }
        String gameToRun = "mollymage"; // TODO get this from url?
        return new Token(gameToRun, serverUrl, matcher.group(1), matcher.group(2));
    }
}
