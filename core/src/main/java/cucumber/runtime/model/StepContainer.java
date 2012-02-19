package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.BasicStatement;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;

public class StepContainer {
    private final List<Step> steps = new ArrayList<Step>();
    protected final CucumberFeature cucumberFeature;
    private final BasicStatement statement;

    public StepContainer(CucumberFeature cucumberFeature, BasicStatement statement) {
        this.cucumberFeature = cucumberFeature;
        this.statement = statement;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void step(Step step) {
        steps.add(step);
    }

    protected void format(Formatter formatter) {
        statement.replay(formatter);
        for (Step step : getSteps()) {
            formatter.step(step);
        }
    }

    public void runSteps(Reporter reporter, Runtime runtime) {
        for (Step step : getSteps()) {
            runStep(step, reporter, runtime);
        }
    }

    public void runStep(Step step, Reporter reporter, Runtime runtime) {
        runtime.runStep(cucumberFeature.getUri(), step, reporter, cucumberFeature.getI18n());
    }
}
