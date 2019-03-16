package io.cucumber.core.backend;

public interface Lookup {
    /**
     * Provides the glue instances used to execute the current scenario.
     *
     * @param glueClass type of instance to be created.
     * @param <T>       type of Glue class
     * @return new Glue instance of type T
     */
    <T> T getInstance(Class<T> glueClass);
}
