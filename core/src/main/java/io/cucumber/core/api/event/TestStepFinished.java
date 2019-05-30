package io.cucumber.core.api.event;

import java.time.Instant;

/**
 * A test step finished event is broadcast when ever a step finishes.
 * <p>
 * A step can either be a {@link PickleStepTestStep} or a
 * {@link HookTestStep} depending on what step was executed.
 * <p>
 * Each test step finished event is followed by an matching
 * {@link TestStepStarted} event for the same step.The order in which
 * these events may be expected is:
 * <pre>
 *     [before hook,]* [[before step hook,]* test step, [after step hook,]*]+, [after hook,]*
 * </pre>
 *
 * @see PickleStepTestStep
 * @see HookTestStep
 */
public final class TestStepFinished extends TestCaseEvent {
    public final TestStep testStep;
    public final Result result;

    public TestStepFinished(Instant timeInstant, TestCase testCase, TestStep testStep, Result result) {
        super(timeInstant, testCase);
        this.testStep = testStep;
        this.result = result;
    }

}
