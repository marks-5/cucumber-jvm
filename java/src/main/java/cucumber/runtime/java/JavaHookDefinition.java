package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

public class JavaHookDefinition implements HookDefinition {

    private final Method method;
    private final TagExpression tagExpression;
    private final int order;
    private final ObjectFactory objectFactory;

    public JavaHookDefinition(Method method, String[] tagExpressions, int order, ObjectFactory objectFactory) {
        this.method = method;
        tagExpression = new TagExpression(asList(tagExpressions));
        this.order = order;
        this.objectFactory = objectFactory;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        // TODO: There is duplication with JavaStepDefinition

        Object target = objectFactory.getInstance(method.getDeclaringClass());
        if (target == null) {
            throw new IllegalStateException("Bug: No target for " + method);
        }
        Object[] args;
        if (method.getParameterTypes().length == 1) {
            args = new Object[]{scenarioResult};
        } else {
            args = new Object[0];
        }
        try {
            method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            throw new CucumberException("Can't invoke " + new MethodFormat().format(method));
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

}
