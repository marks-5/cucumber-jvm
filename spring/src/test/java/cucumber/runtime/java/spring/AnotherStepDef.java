package cucumber.runtime.java.spring;

import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@ContextConfiguration("classpath:cucumber.xml")
public class AnotherStepDef {

    @Autowired
    OneStepDef oneStepDef;

    @Then("^I can read (\\d+) cucumbers from the other step def class$")
    public void i_can_read_cucumbers_from_the_other_step_def_class(int arg1) throws Throwable {
        assertEquals(arg1, oneStepDef.cucumbers);
    }

}
