package io.cucumber.core.runner;

import io.cucumber.core.api.event.StepDefinedEvent;
import io.cucumber.core.backend.DuplicateStepDefinitionException;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.stepexpression.Argument;
import gherkin.pickles.PickleStep;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Function;

final class CachingGlue implements Glue {
    private static final HookComparator ASCENDING = new HookComparator(true);
    private static final HookComparator DESCENDING = new HookComparator(false);
    final Map<String, StepDefinition> stepDefinitionsByPattern = new TreeMap<>();
    final Map<String, StepDefinition> stepDefinitionsByStepText = new HashMap<>();
    final List<HookDefinition> beforeHooks = new ArrayList<>();
    final List<HookDefinition> beforeStepHooks = new ArrayList<>();
    final List<HookDefinition> afterHooks = new ArrayList<>();
    final List<HookDefinition> afterStepHooks = new ArrayList<>();

    private final EventBus bus;
    private final TypeRegistry typeRegistry;

    CachingGlue(EventBus bus, TypeRegistry typeRegistry) {
        this.bus = bus;
        this.typeRegistry = typeRegistry;
    }

    @Override
    public void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinitionFunction) {
        StepDefinition stepDefinition = stepDefinitionFunction.apply(typeRegistry);
        StepDefinition previous = stepDefinitionsByPattern.get(stepDefinition.getPattern());
        if (previous != null) {
            throw new DuplicateStepDefinitionException(previous, stepDefinition);
        }
        stepDefinitionsByPattern.put(stepDefinition.getPattern(), stepDefinition);
        bus.send(new StepDefinedEvent(bus.getInstant(), stepDefinition));
    }

    @Override
    public void addBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(hookDefinition);
        beforeHooks.sort(ASCENDING);
    }

    @Override
    public void addBeforeStepHook(HookDefinition hookDefinition) {
        beforeStepHooks.add(hookDefinition);
        beforeStepHooks.sort(ASCENDING);
    }
    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(hookDefinition);
        afterHooks.sort(DESCENDING);
    }

    @Override
    public void addAfterStepHook(HookDefinition hookDefinition) {
        afterStepHooks.add(hookDefinition);
        afterStepHooks.sort(DESCENDING);
    }

    List<HookDefinition> getBeforeHooks() {
        return new ArrayList<>(beforeHooks);
    }

    List<HookDefinition> getBeforeStepHooks() {
        return new ArrayList<>(beforeStepHooks);
    }

    List<HookDefinition> getAfterHooks() {
        return new ArrayList<>(afterHooks);
    }

    List<HookDefinition> getAfterStepHooks() {
        return new ArrayList<>(afterStepHooks);
    }

    PickleStepDefinitionMatch stepDefinitionMatch(String featurePath, PickleStep step) {
        String stepText = step.getText();

        StepDefinition stepDefinition = stepDefinitionsByStepText.get(stepText);
        if (stepDefinition != null) {
            // Step definition arguments consists of parameters included in the step text and
            // gherkin step arguments (doc string and data table) which are not included in
            // the step text. As such the step definition arguments can not be cached and
            // must be recreated each time.
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            return new PickleStepDefinitionMatch(arguments, stepDefinition, featurePath, step);
        }

        List<PickleStepDefinitionMatch> matches = stepDefinitionMatches(featurePath, step);
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) {
            throw new AmbiguousStepDefinitionsException(step, matches);
        }

        PickleStepDefinitionMatch match = matches.get(0);

        stepDefinitionsByStepText.put(stepText, match.getStepDefinition());

        return match;
    }

    private List<PickleStepDefinitionMatch> stepDefinitionMatches(String featurePath, PickleStep step) {
        List<PickleStepDefinitionMatch> result = new ArrayList<PickleStepDefinitionMatch>();
        for (StepDefinition stepDefinition : stepDefinitionsByPattern.values()) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new PickleStepDefinitionMatch(arguments, stepDefinition, featurePath, step));
            }
        }
        return result;
    }

    void removeScenarioScopedGlue() {
        removeScenarioScopedHooks(beforeHooks);
        removeScenarioScopedHooks(beforeStepHooks);
        removeScenarioScopedHooks(afterHooks);
        removeScenarioScopedHooks(afterStepHooks);
        removeScenariosScopedStepDefinitions(stepDefinitionsByPattern);
        removeScenariosScopedStepDefinitions(stepDefinitionsByStepText);
    }

    private void removeScenarioScopedHooks(List<HookDefinition> beforeHooks) {
        Iterator<HookDefinition> hookIterator = beforeHooks.iterator();
        while (hookIterator.hasNext()) {
            HookDefinition hook = hookIterator.next();
            if (hook instanceof ScenarioScoped) {
                ScenarioScoped scenarioScopedHookDefinition = (ScenarioScoped) hook;
                scenarioScopedHookDefinition.disposeScenarioScope();
                hookIterator.remove();
            }
        }
    }

    private void removeScenariosScopedStepDefinitions(Map<String, StepDefinition> stepDefinitions) {
        Iterator<Map.Entry<String, StepDefinition>> stepDefinitionIterator = stepDefinitions.entrySet().iterator();
        while(stepDefinitionIterator.hasNext()){
            StepDefinition stepDefinition = stepDefinitionIterator.next().getValue();
            if (stepDefinition instanceof ScenarioScoped) {
                ScenarioScoped scenarioScopedStepDefinition = (ScenarioScoped) stepDefinition;
                scenarioScopedStepDefinition.disposeScenarioScope();
                stepDefinitionIterator.remove();
            }
        }
    }
}
