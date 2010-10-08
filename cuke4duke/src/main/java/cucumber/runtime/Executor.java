package cucumber.runtime;

import cucumber.FeatureSource;
import cucumber.StepDefinition;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.Formatter;

import java.util.List;

public class Executor {
    private final FeatureParser parser;

    public Executor(List<StepDefinition> stepDefinitions, Formatter formatter) {
        ExecuteFormatter executeFormatter = new ExecuteFormatter(stepDefinitions, formatter);
        parser = new GherkinParser(executeFormatter);
    }

    public void execute(FeatureSource featureSource) {
        featureSource.execute(parser);
    }
}
