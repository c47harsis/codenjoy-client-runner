package com.codenjoy.clientrunner.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Platform {

    JAVA("pom.xml"),
    NODEJS("package.json"),
    RUBY("Gemfile");

    private final String filename;

    public static Platform of(String filename) {
        return Arrays.stream(Platform.values())
                .filter(platform -> platform.filename.equals(filename))
                .findAny()
                .orElse(null);
    }

    public String getFolderName() {
        return this.name().toLowerCase();
    }
}
