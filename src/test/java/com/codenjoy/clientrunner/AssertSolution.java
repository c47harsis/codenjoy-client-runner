package com.codenjoy.clientrunner;

import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;

import static org.testng.Assert.*;

public class AssertSolution {

    private final SolutionSummary solution;

    public AssertSolution(SolutionSummary solution) {
        this.solution = solution;
    }

    public AssertSolution hasId(int id) {
        assertEquals(id, solution.getId());
        return this;
    }

    public AssertSolution inStatus(Solution.Status status) {
        assertEquals(status.name(), solution.getStatus());

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
