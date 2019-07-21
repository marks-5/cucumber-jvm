package io.cucumber.core.runner;

import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.exception.CucumberException;

import java.util.List;

import static java.util.stream.Collectors.joining;

class DuplicateDefaultDataTableEntryTransformers extends CucumberException {
    DuplicateDefaultDataTableEntryTransformers(List<DefaultDataTableEntryTransformerDefinition> definitions) {
        super(createMessage(definitions));
    }

    private static String createMessage(List<DefaultDataTableEntryTransformerDefinition> definitions) {
        return "There may not be more then one default data table entry. Found:" + definitions.stream()
            .map(d -> d.getLocation(true))
            .collect(joining("\n - ", "\n - ", "\n"));
    }
}
