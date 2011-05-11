package cucumber.runtime.clojure;

import cucumber.runtime.SnippetGenerator;
import gherkin.model.Step;

import java.util.List;

public class ClojureSnippetGenerator extends SnippetGenerator{
    protected ClojureSnippetGenerator(Step step) {
        super(step);
    }

    @Override
    protected String pattern(String name) {
        return super.pattern(name).replaceAll("\"", "\\\\\"");
    }

    @Override
    protected String template() {
        return "({0} #\"{1}\"\n" +
                "  (fn [{3}]\n" +
                "    \" {4}\n" + // TODO: The " should be a ', but that causes a propblem with MessageFormat escaping {4}. Need to read up on MessageFormat docs.
                "    ))\n";
    }

    @Override
    protected String arguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder ();
        for (int n = 0; n < argumentTypes.size(); n++) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append("arg").append(n+1);
        }
        return sb.toString();
    }
}
