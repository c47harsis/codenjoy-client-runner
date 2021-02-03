package com.codenjoy.clientrunner.model;

import lombok.Data;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public final class Token {

    private final String playerId;
    private final String code;
    private final String serverUrl;

    public Token(String serverUrl, String regex){
        Pattern serverUrlPattern = Pattern.compile(regex);
        Matcher matcher = serverUrlPattern.matcher(serverUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Given invalid server URL: '%s' is not match '%s'",
                            serverUrl, regex));
        }
        this.serverUrl = serverUrl;
        playerId = matcher.group(1);
        code = matcher.group(2);
    }

    public boolean isApplicable(Solution solution) {
        return Objects.equals(playerId, solution.getPlayerId())
                && Objects.equals(code, solution.getCode());
    }

}
