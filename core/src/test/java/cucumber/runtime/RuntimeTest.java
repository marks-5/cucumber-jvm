package cucumber.runtime;

import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.runtime.formatter.CucumberJSONFormatter;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cucumber.runtime.TestHelper.feature;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RuntimeTest {

    private static final I18n ENGLISH = new I18n("en");

    @Ignore
    @Test
    public void runs_feature_with_json_formatter() throws Exception {
        CucumberFeature feature = feature("test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background name\n" +
                "    Given b\n" +
                "  Scenario: scenario name\n" +
                "    When s\n");
        StringBuilder out = new StringBuilder();
        JSONFormatter jsonFormatter = new CucumberJSONFormatter(out);
        List<Backend> backends = asList(mock(Backend.class));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), classLoader, backends, runtimeOptions);
        feature.run(jsonFormatter, jsonFormatter, runtime);
        jsonFormatter.done();
        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"feature-name\",\n" +
                "    \"description\": \"\",\n" +
                "    \"name\": \"feature name\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"line\": 1,\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"description\": \"\",\n" +
                "        \"name\": \"background name\",\n" +
                "        \"keyword\": \"Background\",\n" +
                "        \"line\": 2,\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"status\": \"undefined\"\n" +
                "            },\n" +
                "            \"name\": \"b\",\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"line\": 3,\n" +
                "            \"match\": {}\n" +
                "          }\n" +
                "        ],\n" +
                "        \"type\": \"background\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"feature-name;scenario-name\",\n" +
                "        \"description\": \"\",\n" +
                "        \"name\": \"scenario name\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"line\": 4,\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"status\": \"undefined\"\n" +
                "            },\n" +
                "            \"name\": \"s\",\n" +
                "            \"keyword\": \"When \",\n" +
                "            \"line\": 5,\n" +
                "            \"match\": {}\n" +
                "          }\n" +
                "        ],\n" +
                "        \"type\": \"scenario\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"uri\": \"test.feature\"\n" +
                "  }\n" +
                "]";
        assertEquals(expected, out.toString());
    }

    @Test
    public void strict_without_pending_steps_or_errors() {
        Runtime runtime = createStrictRuntime();

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_without_pending_steps_or_errors() {
        Runtime runtime = createNonStrictRuntime();

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_undefined_steps() {
        Runtime runtime = createNonStrictRuntime();
        runtime.undefinedStepsTracker.addUndefinedStep(new Step(null, "Given ", "A", 1, null, null), ENGLISH);
        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void strict_with_undefined_steps() {
        Runtime runtime = createStrictRuntime();
        runtime.undefinedStepsTracker.addUndefinedStep(new Step(null, "Given ", "A", 1, null, null), ENGLISH);
        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_pending_steps_and_no_errors() {
        Runtime runtime = createStrictRuntime();
        runtime.addError(new PendingException());

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_pending_steps() {
        Runtime runtime = createNonStrictRuntime();
        runtime.addError(new PendingException());

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_failed_junit_assumption() {
        Runtime runtime = createNonStrictRuntime();
        runtime.addError(new AssumptionViolatedException("should be treated like pending"));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_errors() {
        Runtime runtime = createNonStrictRuntime();
        runtime.addError(new RuntimeException());

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_errors() {
        Runtime runtime = createStrictRuntime();
        runtime.addError(new RuntimeException());

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void should_pass_if_no_features_are_found() throws IOException {
        ResourceLoader resourceLoader = createResourceLoaderThatFindsNoFeatures();
        Runtime runtime = createStrictRuntime(resourceLoader);

        runtime.run();

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void should_throw_cucumer_exception_if_no_backends_are_found() throws Exception {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            new Runtime(new ClasspathResourceLoader(classLoader), classLoader, Collections.<Backend>emptyList(),
                    new RuntimeOptions(""));
            fail("A CucumberException should have been thrown");
        } catch (CucumberException e) {
            assertEquals("No backends were found. Please make sure you have a backend module on your CLASSPATH.", e.getMessage());
        }
    }

    @Test
    public void should_add_passed_result_to_the_summary_counter() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);
        StepDefinitionMatch match = mock(StepDefinitionMatch.class);

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(reporter, runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 passed)%n" +
                "1 Steps (1 passed)%n")));
    }

    @Test
    public void should_add_pending_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);
        StepDefinitionMatch match = createExceptionThrowingMatch(new PendingException());

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(reporter, runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 pending)%n" +
                "1 Steps (1 pending)%n")));
    }

    @Test
    public void should_add_failed_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);
        StepDefinitionMatch match = createExceptionThrowingMatch(new Exception());

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(reporter, runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 failed)%n" +
                "1 Steps (1 failed)%n")));
    }

    @Test
    public void should_add_ambiguous_match_as_failed_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);

        Runtime runtime = createRuntimeWithMockedGlueWithAmbiguousMatch("--monochrome");
        runScenario(reporter, runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 failed)%n" +
                "1 Steps (1 failed)%n")));
    }

    @Test
    public void should_add_skipped_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);
        StepDefinitionMatch match = createExceptionThrowingMatch(new Exception());

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(reporter, runtime, stepCount(2));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 failed)%n" +
                "2 Steps (1 failed, 1 skipped)%n")));
    }

    @Test
    public void should_add_undefined_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);

        Runtime runtime = createRuntimeWithMockedGlue(null, "--monochrome");
        runScenario(reporter, runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 undefined)%n" +
                "1 Steps (1 undefined)%n")));
    }

    @Test
    public void should_fail_the_scenario_if_before_fails() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);
        StepDefinitionMatch match = mock(StepDefinitionMatch.class);
        HookDefinition hook = createExceptionThrowingHook();

        Runtime runtime = createRuntimeWithMockedGlue(match, hook, true, "--monochrome");
        runScenario(reporter, runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 failed)%n" +
                "1 Steps (1 skipped)%n")));
   }

    @Test
    public void should_fail_the_scenario_if_after_fails() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Reporter reporter = mock(Reporter.class);
        StepDefinitionMatch match = mock(StepDefinitionMatch.class);
        HookDefinition hook = createExceptionThrowingHook();

        Runtime runtime = createRuntimeWithMockedGlue(match, hook, false, "--monochrome");
        runScenario(reporter, runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 failed)%n" +
                "1 Steps (1 passed)%n")));
   }

    @Test
    public void should_make_scenario_name_available_to_hooks() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");
        HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.matches(anyCollectionOf(Tag.class))).thenReturn(true);

        Runtime runtime = createRuntimeWithMockedGlue(mock(StepDefinitionMatch.class), beforeHook, true);
        feature.run(mock(Formatter.class), mock(Reporter.class), runtime);

        ArgumentCaptor<Scenario> capturedScenario = ArgumentCaptor.forClass(Scenario.class);
        verify(beforeHook).execute(capturedScenario.capture());
        assertEquals("scenario name", capturedScenario.getValue().getName());
    }

    @Test
    public void should_make_scenario_id_available_to_hooks() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature",
                "Feature: feature name\n" +
                        "  Scenario: scenario name\n" +
                        "    Given first step\n" +
                        "    When second step\n" +
                        "    Then third step\n");
        HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.matches(anyCollectionOf(Tag.class))).thenReturn(true);

        Runtime runtime = createRuntimeWithMockedGlue(mock(StepDefinitionMatch.class), beforeHook, true);
        feature.run(mock(Formatter.class), mock(Reporter.class), runtime);

        ArgumentCaptor<Scenario> capturedScenario = ArgumentCaptor.forClass(Scenario.class);
        verify(beforeHook).execute(capturedScenario.capture());
        assertEquals("feature-name;scenario-name", capturedScenario.getValue().getId());
    }

    private StepDefinitionMatch createExceptionThrowingMatch(Exception exception) throws Throwable {
        StepDefinitionMatch match = mock(StepDefinitionMatch.class);
        doThrow(exception).when(match).runStep((I18n)any());
        return match;
    }

    private HookDefinition createExceptionThrowingHook() throws Throwable {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyCollectionOf(Tag.class))).thenReturn(true);
        doThrow(new Exception()).when(hook).execute((Scenario)any());
        return hook;
    }

    public void runStep(Reporter reporter, Runtime runtime) {
        Step step = mock(Step.class);
        I18n i18n = mock(I18n.class);
        runtime.runStep("<featurePath>", step, reporter, i18n);
    }

    private ResourceLoader createResourceLoaderThatFindsNoFeatures() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(anyString(), eq(".feature"))).thenReturn(Collections.<Resource>emptyList());
        return resourceLoader;
    }

    private Runtime createStrictRuntime() {
        return createRuntime("-g", "anything", "--strict");
    }

    private Runtime createNonStrictRuntime() {
        return createRuntime("-g", "anything");
    }

    private Runtime createStrictRuntime(ResourceLoader resourceLoader) {
        return createRuntime(resourceLoader, Thread.currentThread().getContextClassLoader(), "-g", "anything", "--strict");
    }

    private Runtime createRuntime(String... runtimeArgs) {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = mock(ClassLoader.class);
        return createRuntime(resourceLoader, classLoader, runtimeArgs);
    }

    private Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader, String... runtimeArgs) {
        RuntimeOptions runtimeOptions = new RuntimeOptions(asList(runtimeArgs));
        Backend backend = mock(Backend.class);
        Collection<Backend> backends = Arrays.asList(backend);

        return new Runtime(resourceLoader, classLoader, backends, runtimeOptions);
    }

    private Runtime createRuntimeWithMockedGlue(StepDefinitionMatch match, String... runtimeArgs) {
        return createRuntimeWithMockedGlue(match, false, mock(HookDefinition.class), false, runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlue(StepDefinitionMatch match, HookDefinition hook, boolean isBefore,
            String... runtimeArgs){
        return createRuntimeWithMockedGlue(match, false, hook, isBefore, runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlueWithAmbiguousMatch(String... runtimeArgs) {
        return createRuntimeWithMockedGlue(mock(StepDefinitionMatch.class), true, mock(HookDefinition.class), false, runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlue(StepDefinitionMatch match, boolean isAmbiguous, HookDefinition hook,
            boolean isBefore, String... runtimeArgs) {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = mock(ClassLoader.class);
        RuntimeOptions runtimeOptions = new RuntimeOptions(asList(runtimeArgs));
        Backend backend = mock(Backend.class);
        RuntimeGlue glue = mock(RuntimeGlue.class);
        mockMatch(glue, match, isAmbiguous);
        mockHook(glue, hook, isBefore);
        Collection<Backend> backends = Arrays.asList(backend);

        return new Runtime(resourceLoader, classLoader, backends, runtimeOptions, glue);
    }

    private void mockMatch(RuntimeGlue glue, StepDefinitionMatch match, boolean isAmbiguous) {
        if (isAmbiguous) {
            Exception exception = new AmbiguousStepDefinitionsException(Arrays.asList(match, match));
            doThrow(exception).when(glue).stepDefinitionMatch(anyString(), (Step)any(), (I18n)any());
        } else {
            when(glue.stepDefinitionMatch(anyString(), (Step)any(), (I18n)any())).thenReturn(match);
        }
    }

    private void mockHook(RuntimeGlue glue, HookDefinition hook, boolean isBefore) {
        if (isBefore) {
            when(glue.getBeforeHooks()).thenReturn(Arrays.asList(hook));
        } else {
            when(glue.getAfterHooks()).thenReturn(Arrays.asList(hook));
        }
    }

    private void runScenario(Reporter reporter, Runtime runtime, int stepCount) {
        gherkin.formatter.model.Scenario gherkinScenario = mock(gherkin.formatter.model.Scenario.class);
        runtime.buildBackendWorlds(reporter, Collections.<Tag>emptySet(), gherkinScenario);
        runtime.runBeforeHooks(reporter, Collections.<Tag>emptySet());
        for (int i = 0; i < stepCount; ++i) {
            runStep(reporter, runtime);
        }
        runtime.runAfterHooks(reporter, Collections.<Tag>emptySet());
        runtime.disposeBackendWorlds();
    }

    private int stepCount(int stepCount) {
        return stepCount;
    }
}
