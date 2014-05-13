package cucumber.runtime.java.spring;

import cucumber.api.java.en.Then;

import static org.junit.Assert.assertEquals;

public class ThirdStepDef {
    int cucumbers;

    @Then("^(\\d+) have been pushed to a third step def class$")
    public void have_been_pushed_to_a_third_step_def_class(int arg1) throws Throwable {
        assertEquals(arg1, cucumbers);
    }

}
