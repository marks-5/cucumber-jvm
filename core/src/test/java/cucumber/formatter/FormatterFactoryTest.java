package cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static cucumber.formatter.TempDir.createTempDirectory;
import static cucumber.formatter.TempDir.createTempFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FormatterFactoryTest {
    private FormatterFactory fc = new FormatterFactory();

    @Test
    public void instantiates_junit_formatter_with_file_arg() {
        Formatter formatter = fc.create("junit:some_file.xml");
        assertEquals(JUnitFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_html_formatter_with_dir_arg() throws IOException {
        Formatter formatter = fc.create("html:" + createTempDirectory().getAbsolutePath());
        assertEquals(HTMLFormatter.class, formatter.getClass());
    }

    @Test
    public void fails_to_instantiate_html_formatter_without_dir_arg() throws IOException {
        try {
            fc.create("html");
            fail();
        } catch (CucumberException e) {
            assertEquals("You must supply an output argument to html. Like so: html:output", e.getMessage());
        }
    }

    @Test
    public void instantiates_pretty_formatter_with_file_arg() throws IOException {
        Formatter formatter = fc.create("pretty:" + createTempFile().getAbsolutePath());
        assertEquals(CucumberPrettyFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_pretty_formatter_without_file_arg() {
        Formatter formatter = fc.create("pretty");
        assertEquals(CucumberPrettyFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_usage_formatter_without_file_arg() {
        Formatter formatter = fc.create("usage");
        assertEquals(UsageFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_usage_formatter_with_file_arg() throws IOException {
        Formatter formatter = fc.create("usage:" + createTempFile().getAbsolutePath());
        assertEquals(UsageFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_single_custom_appendable_formatter_with_stdout() {
        WantsAppendable formatter = (WantsAppendable) fc.create("cucumber.formatter.FormatterFactoryTest$WantsAppendable");
        assertEquals(System.out, formatter.out);
        try {
            fc.create("cucumber.formatter.FormatterFactoryTest$WantsAppendable");
            fail();
        } catch(CucumberException expected) {
            assertEquals("Only one formatter can use STDOUT. If you use more than one formatter you must specify output path with FORMAT:PATH", expected.getMessage());
        }
    }

    @Test
    public void instantiates_custom_appendable_formatter_with_stdout_and_file() throws IOException {
        WantsAppendable formatter = (WantsAppendable) fc.create("cucumber.formatter.FormatterFactoryTest$WantsAppendable");
        assertEquals(System.out, formatter.out);

        WantsAppendable formatter2 = (WantsAppendable) fc.create("cucumber.formatter.FormatterFactoryTest$WantsAppendable:" + createTempFile().getAbsolutePath());
        assertEquals(FileWriter.class, formatter2.out.getClass());
    }

    @Test
    public void instantiates_custom_file_formatter() throws IOException {
        WantsFile formatter = (WantsFile) fc.create("cucumber.formatter.FormatterFactoryTest$WantsFile:halp.txt");
        assertEquals("halp.txt", formatter.out.getPath());
    }

    public static class WantsAppendable extends StubFormatter {
        public final Appendable out;

        public WantsAppendable(Appendable out) {
            this.out = out;
        }
    }

    public static class WantsFile extends StubFormatter {
        public final File out;

        public WantsFile(File out) {
            this.out = out;
        }
    }
}
