package cucumber.api.java8;

import cucumber.api.Scenario;

@Deprecated
public interface HookBody {
    void accept(Scenario scenario) throws Throwable;
}
