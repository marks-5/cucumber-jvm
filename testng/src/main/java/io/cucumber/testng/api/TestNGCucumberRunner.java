package io.cucumber.testng.api;

import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestRunStarted;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.runner.TimeService;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.model.FeatureLoader;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsFactory;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.model.CucumberFeature;
import gherkin.events.PickleEvent;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * Glue code for running Cucumber via TestNG.
 */
public class TestNGCucumberRunner {
    private final EventBus bus;
    private final Filters filters;
    private final FeaturePathFeatureSupplier featureSupplier;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final RuntimeOptions runtimeOptions;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the io.cucumber.core.api.options.CucumberOptions and org.testng.annotations.Test annotations
     */
    public TestNGCucumberRunner(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, resourceLoader);
        runtimeOptions = runtimeOptionsFactory.create();

        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(resourceLoader, classFinder, runtimeOptions);
        bus = new TimeServiceEventBus(TimeService.SYSTEM);
        new Plugins(new PluginFactory(), bus, runtimeOptions);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        filters = new Filters(runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions, bus);
    }

    public void runScenario(PickleEvent pickle) throws Throwable {
        //Possibly invoked in a multi-threaded context
        Runner runner = runnerSupplier.get();
        TestCaseResultListener testCaseResultListener = new TestCaseResultListener(runner.getBus(), runtimeOptions.isStrict());
        runner.runPickle(pickle);
        testCaseResultListener.finishExecutionUnit();

        if (!testCaseResultListener.isPassed()) {
            throw testCaseResultListener.getError();
        }
    }

    public void finish() {
        bus.send(new TestRunFinished(bus.getTime()));
    }

    /**
     * @return returns the cucumber scenarios as a two dimensional array of {@link PickleEventWrapper}
     * scenarios combined with their {@link CucumberFeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        try {
            List<Object[]> scenarios = new ArrayList<Object[]>();
            List<CucumberFeature> features = getFeatures();
            for (CucumberFeature feature : features) {
                for (PickleEvent pickle : feature.getPickles()) {
                    if (filters.matchesFilters(pickle)) {
                        scenarios.add(new Object[]{new PickleEventWrapperImpl(pickle),
                            new CucumberFeatureWrapperImpl(feature)});
                    }
                }
            }
            return scenarios.toArray(new Object[][]{});
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e), null}};
        }
    }

    List<CucumberFeature> getFeatures() {
        bus.send(new TestRunStarted(bus.getTime()));
        return featureSupplier.get();
    }
}
