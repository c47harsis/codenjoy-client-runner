package com.codenjoy.clientrunner;

import com.codenjoy.clientrunner.dto.SolutionSummary;
import com.codenjoy.clientrunner.model.Solution;
import com.codenjoy.clientrunner.service.ClientServerServiceTest;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class AssertSolution {

    private SolutionSummary solution;

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
            case NEW :
            case COMPILING :
                assertNotSame(null, solution.getCreated());
                assertSame(null, solution.getStarted());
                assertSame(null, solution.getFinished());
                break;
            case RUNNING :
                assertNotSame(null, solution.getCreated());
                assertNotSame(null, solution.getStarted());
                assertSame(null, solution.getFinished());
                break;
            case FINISHED :
            case ERROR :
            case KILLED :
                assertNotSame(null, solution.getCreated());
                assertNotSame(null, solution.getStarted());
                assertNotSame(null, solution.getFinished());
                break;

        }
        return this;
    }
}
