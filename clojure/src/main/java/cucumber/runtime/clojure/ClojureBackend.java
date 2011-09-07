package cucumber.runtime.clojure;

import clojure.lang.AFunction;
import clojure.lang.RT;
import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClojureBackend implements Backend {
    private static ClojureBackend instance;

    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public ClojureBackend(List<String> scriptPaths) {
        instance = this;
        try {
            defineStepDefinitions(scriptPaths);
        } catch (Exception e) {
            throw new CucumberException("Failed to define Cloure Step Definitions", e);
        }
    }

    private void defineStepDefinitions(List<String> scriptPaths) throws Exception {
        RT.load("cucumber/runtime/clojure/dsl");
        for (String scriptPath : scriptPaths) {
            Resources.scan(scriptPath.replace('.', '/'), ".clj", new Consumer() {
                public void consume(Resource resource) {
                    try {
                        RT.load(resource.getPath().replaceAll(".clj$", ""));
                    } catch (Exception e) {
                        throw new CucumberException("Failed to parse file " + resource.getPath(), e);
                    }
                }
            });
        }
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newWorld() {
    }

    public void disposeWorld() {
    }

    public String getSnippet(Step step) {
        return new ClojureSnippetGenerator(step).getSnippet();
    }

    private StackTraceElement stepDefLocation(String interpreterClassName, String interpreterMethodName) {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement element = stackTraceElements[i];
            if (element.getClassName().equals(interpreterClassName) && element.getMethodName().equals(interpreterMethodName)) {
                return stackTraceElements[i - 1];
            }
        }
        throw new CucumberException("Couldn't find location for step definition");
    }

    public static void addStepDefinition(Pattern regexp, AFunction body) {
        StackTraceElement location = instance.stepDefLocation("clojure.lang.Compiler", "eval");
        instance.stepDefinitions.add(new ClojureStepDefinition(regexp, body, location));
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return new ArrayList<HookDefinition>();
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return new ArrayList<HookDefinition>();
    }
}
