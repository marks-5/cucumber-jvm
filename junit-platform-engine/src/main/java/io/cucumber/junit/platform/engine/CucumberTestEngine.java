package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

import static io.cucumber.junit.platform.engine.Constants.PARALLEL_CONFIG_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;

public final class CucumberTestEngine extends HierarchicalTestEngine<CucumberEngineExecutionContext> {

    @Override
    public String getId() {
        return "cucumber";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        CucumberEngineDescriptor engineDescriptor = new CucumberEngineDescriptor(uniqueId);
        new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
        return engineDescriptor;
    }

    @Override
    protected CucumberEngineExecutionContext createExecutionContext(ExecutionRequest request) {
        return new CucumberEngineExecutionContext(request.getConfigurationParameters());
    }

    @Override
    protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
        ConfigurationParameters config = request.getConfigurationParameters();
        if (config.getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME).orElse(false)) {
            return new ForkJoinPoolHierarchicalTestExecutorService(
                new PrefixedConfigurationParameters(config, PARALLEL_CONFIG_PREFIX));
        }
        return super.createExecutorService(request);
    }

}
