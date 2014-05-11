package cucumber.runtime.java.spring_contextconfig;

import cucumber.api.CucumberOptions;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring_contextconfig",
                "cucumber.runtime.java.spring",
                "cucumber.api.spring"},
        features = {"classpath:cucumber/runtime/java/spring/cukes.feature",
                "classpath:cucumber/runtime/java/spring/xmlBasedSpring.feature",
                "classpath:cucumber/runtime/java/spring/stepdefInjection.feature",
                "classpath:cucumber/runtime/java/spring/transaction.feature"})
public class RunCukesTest {
}
