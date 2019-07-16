package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface Glue {

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeHook(HookDefinition beforeHook);

    void addAfterHook(HookDefinition afterHook);

    void addBeforeStepHook(HookDefinition beforeStepHook);

    void addAfterStepHook(HookDefinition afterStepHook);

    void addParameterType(ParameterTypeDefinition parameterTypeDefinition);

    void addDataTableType(DataTableTypeDefinition dataTableTypeDefinition);

}
