package com.codenjoy.clientrunner.dto;

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

import com.codenjoy.clientrunner.model.Solution;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolutionSummary {

    public static final String TIME_PATTERN = "hh:mm:ss";

    private int id;
    private String status;

    @JsonFormat(shape = STRING, pattern = TIME_PATTERN)
    private LocalDateTime created;

    @JsonFormat(shape = STRING, pattern = TIME_PATTERN)
    private LocalDateTime started;

    @JsonFormat(shape = STRING, pattern = TIME_PATTERN)
    private LocalDateTime finished;

    public SolutionSummary(Solution solution) {
        created = solution.getCreated();
        finished = solution.getFinished();
        id = solution.getId();
        started = solution.getStarted();
        status = solution.getStatus().toString();
    }
}
