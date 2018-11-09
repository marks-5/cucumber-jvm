package io.cucumber.core.backend;

import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;

public interface BackendProviderService {

    Backend create(ObjectFactory objectFactory, ResourceLoader resourceLoader, TypeRegistry typeRegistry);

}
