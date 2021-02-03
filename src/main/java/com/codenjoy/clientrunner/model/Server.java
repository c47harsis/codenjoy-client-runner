package com.codenjoy.clientrunner.model;

import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public final class Server {

    private final String playerId;
    private final String code;
    private final String server;

    public Server(String server, String regex){
        Pattern serverUrlPattern = Pattern.compile(regex);
        Matcher matcher = serverUrlPattern.matcher(server);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Given invalid server URL: '%s' is not match '%s'",
                            server, regex));
        }
        this.server = server;
        playerId = matcher.group(1);
        code = matcher.group(2);
    }
}
