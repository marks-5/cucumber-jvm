package io.cucumber.junit.api;

class SkippedThrowable extends Throwable {
    private static final long serialVersionUID = 1L;

    SkippedThrowable(NotificationLevel scenarioOrStep) {
        super(String.format("This %s is skipped", scenarioOrStep.lowerCaseName()), null, false, false);
    }

    enum NotificationLevel {
        SCENARIO,
        STEP;

        String lowerCaseName() {
            return name().toLowerCase();
        }
    }
}
