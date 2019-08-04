package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JavaStepDefinitionTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) JavaStepDefinitionTest.this;
        }
    };

    private String argument;

    @Test
    public void can_define_step() throws Throwable {
        Method method = JavaStepDefinitionTest.class.getMethod("one_string_argument", String.class);
        JavaStepDefinition definition = new JavaStepDefinition(method, "three (.*) mice", 0, lookup);
        definition.execute(new Object[]{"one_string_argument"});
        assertThat(argument, is("one_string_argument"));
    }

    public void one_string_argument(String argument) {
        this.argument = argument;
    }

    @Test
    public void can_provide_location_of_step() throws Throwable {
        Method method = JavaStepDefinitionTest.class.getMethod("method_throws");
        JavaStepDefinition definition = new JavaStepDefinition(method, "three (.*) mice", 0, lookup);
        PendingException exception = assertThrows(PendingException.class, () -> definition.execute(new Object[0]));
        Optional<StackTraceElement> match = stream(exception.getStackTrace()).filter(definition::isDefinedAt).findFirst();
        StackTraceElement stackTraceElement = match.get();
        assertThat(stackTraceElement.getMethodName(), is("method_throws"));
        assertThat(stackTraceElement.getClassName(), is(JavaStepDefinitionTest.class.getName()));
    }

    public void method_throws() {
        throw new PendingException();
    }

}
