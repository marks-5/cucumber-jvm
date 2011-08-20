package cucumber.cli;

import cucumber.runtime.Runtime;
import gherkin.formatter.PrettyFormatter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Main {
    private static final String USAGE = "HELP";
    private static final String VERSION = "1.0.0"; // TODO: get this from a file

    public static void main(String[] argv) {
        Runtime runtime = null;

        List<String> filesOrDirs = new ArrayList<String>();

        List<String> args = new ArrayList<String>(asList(argv));
        while (!args.isEmpty()) {
            String arg = args.remove(0);

            if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println(USAGE);
                System.exit(0);
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println(VERSION);
                System.exit(0);
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String packageNameOrScriptPrefix = args.remove(0);
                runtime = new Runtime(packageNameOrScriptPrefix);
            } else {
                filesOrDirs.add(arg);
            }
        }
        if (runtime == null) {
            System.out.println("Missing option: --glue");
            System.exit(1);
        }

        Runner runner = new Runner(runtime, filesOrDirs);

        PrettyFormatter prettyFormatter = new PrettyFormatter(System.out, false, true);

        runner.run(prettyFormatter, prettyFormatter);

        List<String> snippets = runtime.getSnippets();
        if (!snippets.isEmpty()) {
            System.out.println();
            System.out.println("You can implement missing steps with the snippets below:");
            System.out.println();
            for (String snippet : snippets) {
                System.out.println(snippet);
            }
        }
    }
}
