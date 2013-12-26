package cucumber.api.testng;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;

import java.util.List;

/**
 * Glue code for running Cucumber via TestNG.
 */
public class TestNGCucumberRunner {
    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;
    private ClassFinder classFinder;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     */
    public TestNGCucumberRunner(Class clazz) {
        classLoader = clazz.getClassLoader();
        resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        runtimeOptions = runtimeOptionsFactory.create();

        TestNgReporter reporter = new TestNgReporter(System.out);
        runtimeOptions.addPlugin(reporter);
        classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
    }

    /**
     * Run the Cucumber features
     */
    public void runCukes() {
        for (CucumberFeature cucumberFeature : getFeatures()) {
            runCucumber(cucumberFeature);
        }
    }

    public void runCucumber(CucumberFeature cucumberFeature) {
        //Runtime is recreated every time to ensure that runtime.getErrors() is empty
        Runtime runtime = createRuntime();

        cucumberFeature.run(
                runtimeOptions.formatter(classLoader),
                runtimeOptions.reporter(classLoader),
                runtime);

        if (!runtime.getErrors().isEmpty()) {
            throw new CucumberException(runtime.getErrors().get(0));
        } else if (runtime.exitStatus() != 0x00) {
            throw new CucumberException("There are pending or undefined steps.");
        }
        // If there is an undefined step, checking for exit status is the only way to conform CucumberOptions.strict contract
        if (runtime.exitStatus() != 0) {
            //runtime.getSnippets()
            throw new CucumberException("There are undefined steps");
        }
    }

    /**
     * Creates new cucumber runtime
     */
    private cucumber.runtime.Runtime createRuntime() {
        return new cucumber.runtime.Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
    }

    /**
     * @return List of detected cucumber features
     */
    public List<CucumberFeature> getFeatures() {
        return runtimeOptions.cucumberFeatures(resourceLoader);
    }

}
