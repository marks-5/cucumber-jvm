package cuke4duke.internal.clj;

import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.StepArgument;
import cuke4duke.internal.language.JdkPatternArgumentMatcher;
import org.jruby.RubyArray;

import java.util.List;
import java.util.regex.Pattern;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import clojure.lang.AFunction;

public class CljStepDefinition implements StepDefinition {
    private final Pattern regexp;
    private final AFunction closure;

    public CljStepDefinition(Pattern regexp, AFunction closure) {
        this.regexp = regexp;
        this.closure = closure;
    }

    public String regexp_source() {
        return regexp.pattern();
    }

    public String file_colon_line() {
        return regexp_source();
    }

    public void invoke(RubyArray args) throws Throwable {
        Object[] javaArgs = args.toArray();
        Method functionInvoke = lookupInvokeMethod(javaArgs.length);
        try {
            functionInvoke.invoke(closure, javaArgs);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public List<StepArgument> arguments_from(String stepName) {
        return JdkPatternArgumentMatcher.argumentsFrom(regexp, stepName);
    }

    // Clojure's AFunction.invoke doesn't take varargs :-/
    private Method lookupInvokeMethod(int argCount) throws NoSuchMethodException {
        Class[] parameterTypes = new Class[argCount];
        for(int i = 0; i < argCount; i++) {
            parameterTypes[i] = Object.class;
        }
        return AFunction.class.getMethod("invoke", parameterTypes);
    }
}
