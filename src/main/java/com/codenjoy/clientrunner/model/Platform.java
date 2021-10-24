package com.codenjoy.clientrunner.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Platform {

    PSEUDO("pseudo"), // TODO надо более четкий критерий, т.к. в pseudo есть pom.xml тоже
    JAVA("pom.xml"),
    NODEJS("package.json"),
    RUBY("Gemfile"),
    PYTHON("main.py"),
    GO("main.go"),
    PHP("index.php");

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
