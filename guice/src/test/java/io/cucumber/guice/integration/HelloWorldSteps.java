package io.cucumber.guice.integration;

import cucumber.api.java.en.Given;
import io.cucumber.guice.api.ScenarioScoped;

@ScenarioScoped
public class HelloWorldSteps {
    @Given("I have {int} cukes in my belly")
    public void I_have_cukes_in_my_belly(int n) {
      n++;
    }
}
