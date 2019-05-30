package io.cucumber.core.plugin;

import static java.time.Duration.ZERO;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import gherkin.pickles.PickleLocation;
import io.cucumber.core.api.event.TestSourceRead;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.model.CucumberFeature;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.runner.ClockStub;

public class UndefinedStepsTrackerTest {

    @Test
    public void has_undefined_steps() {
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.handleSnippetsSuggested(uri(), locations(), asList(""));
        assertTrue(undefinedStepsTracker.hasUndefinedSteps());
    }

    @Test
    public void has_no_undefined_steps() {
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        assertFalse(undefinedStepsTracker.hasUndefinedSteps());
    }

    @Test
    public void removes_duplicates() {
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.handleSnippetsSuggested(uri(), locations(), asList("**KEYWORD** ^B$"));
        tracker.handleSnippetsSuggested(uri(), locations(), asList("**KEYWORD** ^B$"));
        assertEquals("[Given ^B$]", tracker.getSnippets().toString());
    }

    @Test
    public void uses_given_when_then_keywords() throws IOException {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given A\n" +
                "    Then B\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(4)), asList("**KEYWORD** ^B$"));
        assertEquals("[Then ^B$]", tracker.getSnippets().toString());
    }

    @Test
    public void converts_and_to_previous_step_keyword() throws IOException {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    When A\n" +
                "    And B\n" +
                "    But C\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(5)), asList("**KEYWORD** ^C$"));
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    @Test
    public void backtrack_into_background_to_find_step_keyword() throws IOException {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background:\n" +
                "    When A\n" +
                "  Scenario: scenario name\n" +
                "    And B\n" +
                "    But C\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(5)), asList("**KEYWORD** ^C$"));
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    private void sendTestSourceRead(EventBus bus, CucumberFeature feature) {
        bus.send(new TestSourceRead(bus.getInstant(), feature.getUri().toString(), feature.getSource()));
    }

    @Test
    public void doesnt_try_to_use_star_keyword() throws IOException {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    When A\n" +
                "    And B\n" +
                "    * C\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(5)), asList("**KEYWORD** ^C$"));
        assertEquals("[When ^C$]", tracker.getSnippets().toString());
    }

    @Test
    public void star_keyword_becomes_given_when_no_previous_step() throws IOException {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    * A\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("path/test.feature"), locations(line(3)), asList("**KEYWORD** ^A$"));
        assertEquals("[Given ^A$]", tracker.getSnippets().toString());
    }

    @Test
    public void snippets_are_generated_for_correct_locale() throws Exception {
        EventBus bus = new TimeServiceEventBus(new ClockStub(ZERO));
        UndefinedStepsTracker tracker = new UndefinedStepsTracker();
        tracker.setEventPublisher(bus);
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
                "#language:ru\n" +
                "Функция:\n" +
                "  Сценарий: \n" +
                "    * Б\n");
        sendTestSourceRead(bus, feature);
        tracker.handleSnippetsSuggested(uri("file:path/test.feature"), locations(line(4)), asList("**KEYWORD** ^Б$"));
        assertEquals("[Допустим ^Б$]", tracker.getSnippets().toString());
    }

    private List<PickleLocation> locations(int line) {
        return asList(new PickleLocation(line, 0));
    }

    private List<PickleLocation> locations() {
        return Collections.emptyList();
    }

    private String uri() {
        return uri("");
    }

    private String uri(String path) {
        return path;
    }

    private int line(int line) {
        return line;
    }

}
