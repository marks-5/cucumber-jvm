package cucumber.examples.java.calculator;

import cucumber.api.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"progress", "json:target/cucumber-report.json"})
public class RunCukesTest {

}
