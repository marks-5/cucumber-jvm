package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class EO {

    public static void Do(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Donitaĵo(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Kaj(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Se(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Sed(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

}
