package io.cucumber.examples.java.calculator;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"progress", "json:target/cucumber-report.json"})
public class RunCukesTest {

}
