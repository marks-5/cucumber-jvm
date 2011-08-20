package cucumber.cli;

import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.io.FileResource;
import cucumber.io.Resource;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Runner {
    private final Runtime runtime;
    private final List<String> filesOrDirs;
    private final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
    private final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);

    public Runner(Runtime runtime, List<String> filesOrDirs) {
        this.runtime = runtime;
        this.filesOrDirs = filesOrDirs;
    }

    public void run(Formatter formatter, Reporter reporter) {
        // Parse all gherkin files and build CucumberFeatures
        traverse(filesOrDirs.toArray(new String[filesOrDirs.size()]));

        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            cucumberFeature.run(runtime, formatter, reporter);
        }
    }

    private void traverse(String[] filesOrDirs) {
        for (String fileOrDir : filesOrDirs) {
            File file = new File(fileOrDir);
            if (file.exists()) {
                // Read it directly from the file system
                traverse(file);
            } else {
                // Read it from the classpath
                Consumer consumer = new Consumer() {
                    @Override
                    public void consume(Resource resource) {
                        builder.parse(resource);
                    }
                };
                if (fileOrDir.endsWith(".feature")) {
                    Classpath.scan(fileOrDir, consumer);
                } else {
                    Classpath.scan(fileOrDir, ".feature", consumer);
                }
            }
        }
    }

    private void traverse(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles();
            for (File file : files) {
                traverse(file);
            }
        } else if (fileOrDir.isFile() && fileOrDir.getName().endsWith(".feature")) {
            addFeature(fileOrDir);
        }
    }

    private void addFeature(File path) {
        File rootDir = path.getParentFile(); // TODO: use current dir, and fix FileResource to deal with incompatible paths
        Resource resource = new FileResource(rootDir, path);
        builder.parse(resource);
    }
}
