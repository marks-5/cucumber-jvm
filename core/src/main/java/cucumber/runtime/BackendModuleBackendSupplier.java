package cucumber.runtime;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import io.cucumber.core.options.RunnerOptions;
import io.cucumber.stepexpression.TypeRegistry;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;


/**
 * Supplies instances of {@link Backend} found by scanning {@code cucumber.runtime} for implementations.
 */
public final class BackendModuleBackendSupplier implements BackendSupplier {

    private final ResourceLoader resourceLoader;
    private final ClassFinder classFinder;
    private final RunnerOptions runnerOptions;
    private final List<String> packages;

    public BackendModuleBackendSupplier(ResourceLoader resourceLoader, ClassFinder classFinder, RunnerOptions runnerOptions) {
        this(resourceLoader, classFinder, runnerOptions, singletonList("cucumber.runtime"));
    }

    BackendModuleBackendSupplier(ResourceLoader resourceLoader, ClassFinder classFinder, RunnerOptions runnerOptions, List<String> packages) {
        this.resourceLoader = resourceLoader;
        this.classFinder = classFinder;
        this.runnerOptions = runnerOptions;
        this.packages = packages;
    }

    @Override
    public Collection<? extends Backend> get() {
        Collection<? extends Backend> backends = loadBackends();
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        return backends;
    }

    private Collection<? extends Backend> loadBackends() {
        Reflections reflections = new Reflections(classFinder);
        TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, MultiLoader.packageName(runnerOptions.getGlue()), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
        TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
        typeRegistryConfigurer.configureTypeRegistry(typeRegistry);

        return reflections.instantiateSubclasses(Backend.class, packages, new Class[]{ResourceLoader.class, TypeRegistry.class}, new Object[]{resourceLoader, typeRegistry});
    }

}
