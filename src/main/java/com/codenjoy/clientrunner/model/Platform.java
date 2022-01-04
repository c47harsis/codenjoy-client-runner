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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Platform {

    PSEUDO("pseudo.mrk"),
    JAVA("java.mrk"),
    SCALA("scala.mrk"),
    KOTLIN("kotlin.mrk"),
    JAVASCRIPT("javascript.mrk"),
    RUBY("ruby.mrk"),
    PYTHON("python.mrk"),
    GO("go.mrk"),
    PHP("php.mrk"),
    CSHARP("csharp.mrk");

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
