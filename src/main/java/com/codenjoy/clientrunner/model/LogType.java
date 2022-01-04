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

import static com.codenjoy.clientrunner.model.Solution.Status.COMPILING;
import static com.codenjoy.clientrunner.model.Solution.Status.RUNNING;

@Getter
@RequiredArgsConstructor
public enum LogType {

    BUILD("build.log", COMPILING),
    RUNTIME("app.log", RUNNING);

    private final String filename;
    private final Solution.Status status;

    public boolean existsWhen(Solution.Status status) {
        return this.status.getStage() <= status.getStage();
    }
}
