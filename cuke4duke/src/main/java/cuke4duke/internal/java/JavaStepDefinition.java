package cuke4duke.internal.java;

import cuke4duke.internal.JRuby;
import cuke4duke.internal.jvmclass.ClassLanguage;
import cuke4duke.internal.jvmclass.ObjectFactory;
import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.JdkPatternArgumentMatcher;
import cuke4duke.internal.language.StepArgument;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class JavaStepDefinition extends AbstractStepDefinition {
    private final ObjectFactory objectFactory;
    private final Method method;
    private final Pattern regexp;
    private final MethodInvoker methodInvoker;
    private final MethodFormat methodFormat;

    public JavaStepDefinition(ClassLanguage programmingLanguage, ObjectFactory objectFactory, Method method, Pattern regexp, MethodFormat methodFormat) throws Throwable {
        super(programmingLanguage);
        this.objectFactory = objectFactory;
        this.method = method;
        this.regexp = regexp;
        this.methodFormat = methodFormat;
        this.methodInvoker = new MethodInvoker(method);
        register();
    }

    public String regexp_source() {
        return regexp.pattern();
    }
    
    public List<StepArgument> arguments_from(String stepName) throws UnsupportedEncodingException {
        return JdkPatternArgumentMatcher.argumentsFrom(regexp, stepName);
    }

    public String file_colon_line() {
        return methodFormat.format(method);
    }

    protected Class<?>[] getParameterTypes(Object[] args) {
        Class<?>[] types = method.getParameterTypes();
        if(types.length != args.length) {
            throw JRuby.cucumberArityMismatchError("Expected " + types.length + " arguments, got " + args.length);
        }
        return types;
    }

    public void invokeWithJavaArgs(Object[] args) throws Throwable {
        Object target = objectFactory.getComponent(method.getDeclaringClass());
        methodInvoker.invoke(target, args);
    }

}
