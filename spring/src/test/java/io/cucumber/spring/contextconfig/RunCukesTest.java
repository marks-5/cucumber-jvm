package io.cucumber.spring.contextconfig;

import cucumber.api.CucumberOptions;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = {"cucumber.runtime.java.spring.contextconfig",
                "cucumber.runtime.java.spring.commonglue",
                "cucumber.api.spring"},
        features = {"classpath:io/cucumber/spring/cukes.feature",
                "classpath:io/cucumber/spring/xmlBasedSpring.feature",
                "classpath:io/cucumber/spring/stepdefInjection.feature",
                "classpath:io/cucumber/spring/transaction.feature"})
public class RunCukesTest {
}
