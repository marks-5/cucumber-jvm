package io.cucumber.core.runner;

import io.cucumber.core.event.Result;
import io.cucumber.core.event.EmbedEvent;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.WriteEvent;
import gherkin.pickles.PickleTag;
import io.cucumber.core.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.max;

class Scenario implements io.cucumber.core.api.Scenario {

    private final List<Result> stepResults = new ArrayList<Result>();
    private final EventBus bus;
    private final TestCase testCase;

    Scenario(EventBus bus, io.cucumber.core.event.TestCase testCase) {
        this.bus = bus;
        this.testCase = testCase;
    }

    public void add(Result result) {
        stepResults.add(result);
    }

    @Override
    public Collection<String> getSourceTagNames() {
        Set<String> result = new HashSet<>();
        for (PickleTag tag : testCase.getTags()) {
            result.add(tag.getName());
        }
        // Has to be a List in order for JRuby to convert to Ruby Array.
        return new ArrayList<>(result);
    }

    @Override
    public Result.Type getStatus() {
        if (stepResults.isEmpty()) {
            return Result.Type.UNDEFINED;
        }

        return max(stepResults, Result.SEVERITY).getStatus();
    }

    @Override
    public boolean isFailed() {
        return getStatus() == Result.Type.FAILED;
    }

    @Override
    public void embed(byte[] data, String mimeType) {
        if (bus != null) {
            bus.send(new EmbedEvent(bus.getInstant(), testCase, data, mimeType));
        }
    }

    @Override
    public void write(String text) {
        if (bus != null) {
            bus.send(new WriteEvent(bus.getInstant(), testCase, text));
        }
    }

    @Override
    public String getName() {
        return testCase.getName();
    }

    @Override
    public String getId() {
        return testCase.getUri() + ":" + getLine();
    }

    @Override
    public String getUri() {
        return testCase.getUri();
    }

    @Override
    public Integer getLine() {
        return testCase.getLine();
    }

    public Throwable getError() {
        if (stepResults.isEmpty()) {
            return null;
        }

        return max(stepResults, Result.SEVERITY).getError();
    }
}
