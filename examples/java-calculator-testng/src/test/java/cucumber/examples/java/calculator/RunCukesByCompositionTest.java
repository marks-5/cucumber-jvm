package cucumber.examples.java.calculator;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.TestNGCucumberRunner;
import org.testng.annotations.Test;

/**
 * An example of using TestNG when the test class does not inherit from 
 * AbstractTestNGCucumberTests.
 */
@CucumberOptions(format = "json:target/cucumber-report.json")
public class RunCukesByCompositionTest extends RunCukesByCompositionBase {

    /**
     * Create one test method that will be invoked by TestNG and invoke the 
     * Cucumber runner within that method.
     */
    @Test
    public void runCukes() {
        new TestNGCucumberRunner(getClass()).runCukes();
    }
}
