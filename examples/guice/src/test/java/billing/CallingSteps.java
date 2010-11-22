package billing;

import com.google.inject.Inject;
import cuke4duke.StepMother;
import cuke4duke.Steps;
import cucumber.annotation.annotation.I18n.EN.When;

public class CallingSteps extends Steps {

    @Inject
    public CallingSteps(StepMother stepMother) {
        super(stepMother);
    }

    @When("^I call another step$")
    public void iCallAnotherStep() {
        Given("it is magic");
    }
}
