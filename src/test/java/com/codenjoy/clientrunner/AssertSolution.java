package com.codenjoy.clientrunner;

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

import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;

import static org.testng.Assert.*;

public class AssertSolution {

    private final SolutionSummary solution;

    public AssertSolution(SolutionSummary solution) {
        this.solution = solution;
    }

    public AssertSolution hasId(int id) {
        assertEquals(solution.getId(), id);
        return this;
    }

    public AssertSolution inStatus(Solution.Status status) {
        assertEquals(solution.getStatus(), status.name());

        switch (Solution.Status.valueOf(solution.getStatus())) {
            case NEW:
            case COMPILING:
                assertNotNull(solution.getCreated());
                assertNull(solution.getStarted());
                assertNull(solution.getFinished());
                break;
            case RUNNING:
                assertNotNull(solution.getCreated());
                assertNotNull(solution.getStarted());
                assertNull(solution.getFinished());
                break;
            case FINISHED:
            case ERROR:
            case KILLED:
                assertNotNull(solution.getCreated());
                assertNotNull(solution.getStarted());
                assertNotNull(solution.getFinished());
                break;
            default:
                fail("Solution has invalid status: " + solution.getStatus());
        }
        return this;
    }
}
