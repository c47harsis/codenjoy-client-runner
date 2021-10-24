package com.codenjoy.clientrunner.model;

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
