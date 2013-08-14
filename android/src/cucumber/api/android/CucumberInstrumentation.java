package cucumber.api.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.android.AndroidFormatter;
import cucumber.runtime.android.AndroidObjectFactory;
import cucumber.runtime.android.AndroidResourceLoader;
import cucumber.runtime.android.DexReflections;
import cucumber.runtime.io.Reflections;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import dalvik.system.DexFile;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.TagStatement;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CucumberInstrumentation extends Instrumentation {
    public static final String ARGUMENT_TEST_CLASS = "class";
    public static final String ARGUMENT_TEST_PACKAGE = "package";
    public static final String REPORT_VALUE_ID = "InstrumentationTestRunner";
    public static final String REPORT_KEY_NUM_TOTAL = "numtests";
    public static final String REPORT_KEY_NUM_CURRENT = "current";
    public static final String REPORT_KEY_NAME_CLASS = "class";
    public static final String REPORT_KEY_NAME_TEST = "test";
    public static final int REPORT_VALUE_RESULT_START = 1;
    public static final int REPORT_VALUE_RESULT_ERROR = -1;
    public static final int REPORT_VALUE_RESULT_FAILURE = -2;
    public static final String REPORT_KEY_STACK = "stack";
    public static final String TAG = "cucumber-android";
    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;
    private Runtime runtime;
    private String packageOfTests;
    private String features;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        if (arguments == null) {
            throw new CucumberException("No arguments");
        }
        Context context = getContext();
        classLoader = context.getClassLoader();

        String apkPath = context.getPackageCodePath();
        Reflections reflections = new DexReflections(newDexFile(apkPath));

        // For glue and features either use the provided arguments or try to find a RunWithCucumber annotated class.
        // If nothing works, default values will be used instead.
        if (arguments.containsKey(ARGUMENT_TEST_CLASS) || arguments.containsKey(ARGUMENT_TEST_PACKAGE)) {

            String testClass = arguments.getString(ARGUMENT_TEST_CLASS);
            testClass = testClass != null ? testClass : "null";
            packageOfTests = arguments.getString(ARGUMENT_TEST_PACKAGE);

            try {
                Class<?> clazz = classLoader.loadClass(testClass);
                boolean annotationWasPresent = readRunWithCucumberAnnotation(clazz);

                // If the class is not RunWithCucumber annotated, maybe it's Cucumber annotated?
                if (!annotationWasPresent) {
                    SEARCH_ANNOTATION:
                    for (Method m : clazz.getMethods()) {
                        for (Annotation a : m.getAnnotations()) {
                            if (a.annotationType().getName().startsWith("cucumber") && packageOfTests == null) {
                                packageOfTests = testClass.substring(0, testClass.lastIndexOf("."));
                                break SEARCH_ANNOTATION;
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                Log.w(TAG, e.toString());
            }
        } else {
            for (Class<?> clazz : reflections.getDescendants(Object.class, context.getPackageName())) {
                if (readRunWithCucumberAnnotation(clazz)) break;
            }
        }

        Properties properties = new Properties();
        packageOfTests = packageOfTests != null ? packageOfTests : defaultGlue();
        features = features != null ? features : defaultFeatures();

        properties.setProperty("cucumber.options", String.format("-g %s %s", packageOfTests, features));
        runtimeOptions = new RuntimeOptions(properties);

        resourceLoader = new AndroidResourceLoader(context);

        List<Backend> backends = new ArrayList<Backend>();
        backends.add(new JavaBackend(new AndroidObjectFactory(this), reflections));
        runtime = new Runtime(resourceLoader, classLoader, backends, runtimeOptions);

        start();
    }

    private DexFile newDexFile(String apkPath) {
        try {
            return new DexFile(apkPath);
        } catch (IOException e) {
            throw new CucumberException("Failed to open " + apkPath);
        }
    }

    /**
     * @return true if the class is RunWithCucumber annotated, false otherwise
     */
    private boolean readRunWithCucumberAnnotation(Class<?> clazz) {
        RunWithCucumber annotation = clazz.getAnnotation(RunWithCucumber.class);
        if (annotation != null) {
            // isEmpty() only available in Android API 9+
            packageOfTests = annotation.glue().equals("") ? defaultGlue() : annotation.glue();
            features = annotation.features().equals("") ? defaultFeatures() : annotation.features();
            return true;
        }
        return false;
    }

    private String defaultFeatures() {
        return "features";
    }

    private String defaultGlue() {
        return getContext().getPackageName();
    }

    @Override
    public void onStart() {
        Looper.prepare();

        List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        int numScenarios = 0;

        for (CucumberFeature feature : cucumberFeatures) {
            for (CucumberTagStatement statement : feature.getFeatureElements()) {
                if (statement instanceof CucumberScenario) {
                    numScenarios++;
                } else if (statement instanceof CucumberScenarioOutline) {
                    for (CucumberExamples examples : ((CucumberScenarioOutline) statement).getCucumberExamplesList()) {
                        for (ExamplesTableRow row : examples.getExamples().getRows()) {
                            numScenarios++;
                        }
                    }
                    numScenarios--; // subtract table header
                }
            }
        }

        AndroidReporter reporter = new AndroidReporter(numScenarios);
        runtimeOptions.getFormatters().clear();
        runtimeOptions.getFormatters().add(reporter);

        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            Formatter formatter = runtimeOptions.formatter(classLoader);
            cucumberFeature.run(formatter, reporter, runtime);
        }
        Formatter formatter = runtimeOptions.formatter(classLoader);

        formatter.done();
        printSummary();
        formatter.close();

        finish(Activity.RESULT_OK, new Bundle());
    }

    private void printSummary() {
        for (Throwable t : runtime.getErrors()) {
            Log.e(TAG, t.toString());
        }
        for (String s : runtime.getSnippets()) {
            Log.w(TAG, s);
        }
    }

    /**
     * This class reports the current test-state back to the framework.
     * It also wraps the AndroidFormatter to intercept important callbacks.
     */
    private class AndroidReporter implements Formatter, Reporter {
        private final AndroidFormatter formatter;
        private final Bundle resultTemplate;
        private Bundle testResult;
        private int scenarioCounter;
        private int resultCode; // aka exit status
        private Feature currentFeature;
        private Step currentStep;

        public AndroidReporter(int numTests) {
            formatter = new AndroidFormatter(TAG);
            resultTemplate = new Bundle();
            resultTemplate.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            resultTemplate.putInt(REPORT_KEY_NUM_TOTAL, numTests);
        }

        @Override
        public void uri(String uri) {
            formatter.uri(uri);
        }

        @Override
        public void feature(Feature feature) {
            currentFeature = feature;
            formatter.feature(feature);
        }

        @Override
        public void background(Background background) {
            formatter.background(background);
        }

        @Override
        public void scenario(Scenario scenario) {
            reportLastResult();
            formatter.scenario(scenario);
            beginScenario(scenario);
        }

        @Override
        public void scenarioOutline(ScenarioOutline scenarioOutline) {
            reportLastResult();
            formatter.scenarioOutline(scenarioOutline);
            beginScenario(scenarioOutline);
        }

        @Override
        public void examples(Examples examples) {
            formatter.examples(examples);
        }

        @Override
        public void step(Step step) {
            currentStep = step;
            formatter.step(step);
        }

        @Override
        public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
            formatter.syntaxError(state, event, legalEvents, uri, line);
        }

        @Override
        public void eof() {
            reportLastResult();
            formatter.eof();
        }

        @Override
        public void done() {
            formatter.done();
        }

        @Override
        public void close() {
            formatter.close();
        }

        @Override
        public void embedding(String mimeType, byte[] data) {
        }

        @Override
        public void write(String text) {
        }

        @Override
        public void before(Match match, Result result) {
        }

        @Override
        public void after(Match match, Result result) {
        }

        @Override
        public void match(Match match) {
        }

        private void beginScenario(TagStatement scenario) {
            String testClass = String.format("%s %s", currentFeature.getKeyword(), currentFeature.getName());
            String testName = String.format("%s %s", scenario.getKeyword(), scenario.getName());
            testResult = new Bundle(resultTemplate);
            testResult.putString(REPORT_KEY_NAME_CLASS, testClass);
            testResult.putString(REPORT_KEY_NAME_TEST, testName);
            testResult.putInt(REPORT_KEY_NUM_CURRENT, ++scenarioCounter);

            testResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, String.format("\n%s:", testClass));

            sendStatus(REPORT_VALUE_RESULT_START, testResult);
            resultCode = 0;
        }

        @Override
        public void result(Result result) {
            if (result.getError() != null) {
                // If the result contains an error, report a failure.
                testResult.putString(REPORT_KEY_STACK, result.getErrorMessage());
                resultCode = REPORT_VALUE_RESULT_FAILURE;
                testResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, result.getErrorMessage());
            } else if (result.getStatus().equals("undefined")) {
                // There was a missing step definition, report an error.
                List<String> snippets = runtime.getSnippets();
                String report = String.format("Missing step-definition\n\n%s\nfor step '%s'",
                        snippets.get(snippets.size() - 1),
                        currentStep.getName());
                testResult.putString(REPORT_KEY_STACK, report);
                resultCode = REPORT_VALUE_RESULT_ERROR;
                testResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                        String.format("Missing step-definition: %s", currentStep.getName()));
            }
        }

        private void reportLastResult() {
            if (scenarioCounter != 0) {
                if (resultCode == 0) {
                    testResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, ".");
                }
                sendStatus(resultCode, testResult);
            }
        }
    }
}
