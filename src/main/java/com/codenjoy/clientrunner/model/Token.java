package com.codenjoy.clientrunner.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Token {

    private final String serverUrl;
    private final String playerId;
    private final String code;

    public static Token from(String serverUrl, String regex) {
        Pattern serverUrlPattern = Pattern.compile(regex);
        Matcher matcher = serverUrlPattern.matcher(serverUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Given invalid server URL: '%s' is not match '%s'", serverUrl, regex));
        }
        return new Token(serverUrl, matcher.group(1), matcher.group(2));
    }
}
