package cucumber;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.util.List;

public interface StepDefinition {
    List<Argument> argumentsFrom(Step step);
    String getLocation();
    Class<?>[] getParameterTypes();
    void execute(Object[] args) throws Throwable;
    boolean isDefinedAt(StackTraceElement stackTraceElement);
}
