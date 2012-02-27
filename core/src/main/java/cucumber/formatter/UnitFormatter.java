package cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Uladzimir Mihura
 *         Date: 2/24/12
 *         Time: 4:02 PM
 */
public class UnitFormatter implements Formatter, Reporter {
    private FileWriter out;
    private Document doc;
    private Element rootElement;
    private TestCase testCase;


    public UnitFormatter(FileWriter out) {
        this.out = out;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            rootElement = doc.createElement("testsuite");
            doc.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            throw new CucumberException("Error while processing unit report", e);
        }
    }


    @Override
    public void feature(Feature feature) {
        TestCase.feature = feature;
    }

    @Override
    public void background(Background background) {
        testCase = new TestCase();
    }

    @Override
    public void scenario(Scenario scenario) {
        if (testCase != null) {
            testCase.scenario = scenario;
        } else {
            testCase = new TestCase(scenario);
        }

        increaseAttributeValue(rootElement, "tests");
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void step(Step step) {
        if (testCase != null) testCase.steps.add(step);
    }


    @Override
    public void done() {
        try {
            //set up a transformer
            rootElement.setAttribute("failed",String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(out);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
        } catch (TransformerException e) {
            new CucumberException("Error while transforming.", e);
        }
    }


    @Override
    public void result(Result result) {
        testCase.results.add(result);

        if (testCase.scenario != null && testCase.results.size() == testCase.steps.size()) {
            rootElement.appendChild(testCase.writeTo(doc));
            testCase = null;
        }
    }

    private void increaseAttributeValue(Element element, String attribute) {
        int value = 0;
        if (element.hasAttribute(attribute)) {
            value = Integer.parseInt(element.getAttribute(attribute));
        }
        element.setAttribute(attribute, String.valueOf(++value));

    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void embedding(String mimeType, InputStream data) {
    }

    @Override
    public void write(String text) {
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void close() {

    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
    }

    private static class TestCase {
        private TestCase(Scenario scenario) {
            this.scenario = scenario;
        }

        private TestCase() {
        }

        Scenario scenario;
        static Feature feature;
        List<Step> steps = new ArrayList<Step>();
        List<Result> results = new ArrayList<Result>();

        private Element writeTo(Document doc) {
            Element tc = doc.createElement("testcase");
            tc.setAttribute("classname", "Feature:" + feature.getName());
            tc.setAttribute("name", "Scenario:" + scenario.getName());
            long time = 0;
            for (Result r : results) {
                time += r.getDuration() != null ? r.getDuration() : 0;
            }
            tc.setAttribute("time", String.valueOf(time));

            StringBuilder sb = new StringBuilder();
            Result skipped = null, failed = null;
            for (int i = 0; i < steps.size(); i++) {
                int length = sb.length();
                Step step = steps.get(i);
                Result result = results.get(i);
                if ("failed".equals(result.getStatus())) failed = result;
                if ("undefined".equals(result.getStatus()) || "pending".equals(result.getStatus())) skipped = result;
                sb.append(steps.get(i).getKeyword());
                sb.append(steps.get(i).getName());
                for (int j = 0; sb.length() - length + j < 140; j++) sb.append(".");
                sb.append(result.getStatus());
                sb.append("\n");
            }
            Element child;
            if (failed != null) {
                sb.append("\nStackTrace:\n");
                StringWriter sw = new StringWriter();
                failed.getError().printStackTrace(new PrintWriter(sw));
                sb.append(sw.toString());
                child = doc.createElement("failure");
                child.setAttribute("message", failed.getErrorMessage());
                child.appendChild(doc.createCDATASection(sb.toString()));
            } else if (skipped != null) {
                child = doc.createElement("skipped");
                child.appendChild(doc.createCDATASection(sb.toString()));
            } else {
                child = doc.createElement("system-out");
                child.appendChild(doc.createCDATASection(sb.toString()));
            }
            tc.appendChild(child);
            return tc;
        }
    }

}
