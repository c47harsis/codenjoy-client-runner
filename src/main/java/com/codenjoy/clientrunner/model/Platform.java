package com.codenjoy.clientrunner.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Platform {

    PSEUDO("pseudo.mrk"),
    JAVA("java.mrk"),
    NODEJS("javascript.mrk"),
    RUBY("ruby.mrk"),
    PYTHON("python.mrk"),
    GO("go.mrk"),
    PHP("php.mrk");

    private final String filename;

    public static Platform of(String filename) {
        return Arrays.stream(Platform.values())
                .filter(platform -> platform.getFilename().equals(filename))
                .findAny()
                .orElse(null);
    }

    public String getFolderName() {
        return this.name().toLowerCase();
    }
}
