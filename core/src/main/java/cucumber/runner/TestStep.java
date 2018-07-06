package cucumber.runner;

import cucumber.api.Pending;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestCase;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runtime.AmbiguousStepDefinitionsException;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.UndefinedStepDefinitionException;

import java.util.Arrays;

abstract class TestStep implements cucumber.api.TestStep {
    private static final String[] ASSUMPTION_VIOLATED_EXCEPTIONS = {
        "org.junit.AssumptionViolatedException",
        "org.junit.internal.AssumptionViolatedException",
        "org.testng.SkipException"
    };

    static {
        Arrays.sort(ASSUMPTION_VIOLATED_EXCEPTIONS);
    }

    private final StepDefinitionMatch stepDefinitionMatch;

    TestStep(StepDefinitionMatch stepDefinitionMatch) {
        this.stepDefinitionMatch = stepDefinitionMatch;
    }

    @Override
    public String getCodeLocation() {
        return stepDefinitionMatch.getCodeLocation();
    }

    Result run(TestCase testCase, EventBus bus, String language, Scenario scenario, boolean skipSteps) {
        Long startTime = bus.getTime();
        bus.send(new TestStepStarted(startTime, testCase, this));
        Result.Type status;
        Throwable error = null;
        try {
            status = executeStep(language, scenario, skipSteps);
        } catch (Throwable t) {
            error = t;
            status = mapThrowableToStatus(t);
        }
        Long stopTime = bus.getTime();
        Result result = mapStatusToResult(status, error, stopTime - startTime);
        bus.send(new TestStepFinished(stopTime, testCase, this, result));
        return result;
    }

    private Result.Type executeStep(String language, Scenario scenario, boolean skipSteps) throws Throwable {
        if (!skipSteps) {
            stepDefinitionMatch.runStep(language, scenario);
            return Result.Type.PASSED;
        } else {
            stepDefinitionMatch.dryRunStep(language, scenario);
            return Result.Type.SKIPPED;
        }
    }

    private Result.Type mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(Pending.class)) {
            return Result.Type.PENDING;
        }
        if (Arrays.binarySearch(ASSUMPTION_VIOLATED_EXCEPTIONS, t.getClass().getName()) >= 0) {
            return Result.Type.SKIPPED;
        }
        if (t.getClass() == UndefinedStepDefinitionException.class) {
            return Result.Type.UNDEFINED;
        }
        if (t.getClass() == AmbiguousStepDefinitionsException.class) {
            return Result.Type.AMBIGUOUS;
        }
        return Result.Type.FAILED;
    }

    private Result mapStatusToResult(Result.Type status, Throwable error, long duration) {
        if (status == Result.Type.SKIPPED && error == null) {
            return Result.SKIPPED;
        }
        if (status == Result.Type.UNDEFINED) {
            return Result.UNDEFINED;
        }
        return new Result(status, duration, error);
    }
}
