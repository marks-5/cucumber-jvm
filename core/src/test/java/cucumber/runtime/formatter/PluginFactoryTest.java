package cucumber.runtime.formatter;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.io.UTF8OutputStreamWriter;
import gherkin.formatter.model.Result;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class PluginFactoryTest {
    private PluginFactory fc = new PluginFactory();

    @Test
    public void instantiates_null_plugin() {
        Object plugin = fc.create("null");
        assertEquals(NullFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_junit_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create("junit:" + File.createTempFile("cucumber", "xml"));
        assertEquals(JUnitFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_html_plugin_with_dir_arg() throws IOException {
        Object plugin = fc.create("html:" + TempDir.createTempDirectory().getAbsolutePath());
        assertEquals(HTMLFormatter.class, plugin.getClass());
    }

    @Test
    public void fails_to_instantiate_html_plugin_without_dir_arg() throws IOException {
        try {
            fc.create("html");
            fail();
        } catch (CucumberException e) {
            assertEquals("You must supply an output argument to html. Like so: html:output", e.getMessage());
        }
    }

    @Test
    public void instantiates_pretty_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create("pretty:" + Utils.toURL(TempDir.createTempFile().getAbsolutePath()));
        assertEquals(CucumberPrettyFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_pretty_plugin_without_file_arg() {
        Object plugin = fc.create("pretty");
        assertEquals(CucumberPrettyFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_usage_plugin_without_file_arg() {
        Object plugin = fc.create("usage");
        assertEquals(UsageFormatter.class, plugin.getClass());
    }

    @Test
    public void instantiates_usage_plugin_with_file_arg() throws IOException {
        Object plugin = fc.create("usage:" + TempDir.createTempFile().getAbsolutePath());
        assertEquals(UsageFormatter.class, plugin.getClass());
    }

    @Test
    public void plugin_does_not_buffer_its_output() throws IOException {
        PrintStream previousSystemOut = System.out;
        OutputStream mockSystemOut = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(mockSystemOut));

            // Need to create a new plugin factory here since we need it to pick up the new value of System.out
            fc = new PluginFactory();

            ProgressFormatter plugin = (ProgressFormatter) fc.create("progress");

            plugin.result(new Result("passed", null, null));

            assertThat(mockSystemOut.toString(), is(not("")));
        } finally {
            System.setOut(previousSystemOut);
        }
    }

    @Test
    public void instantiates_single_custom_appendable_plugin_with_stdout() {
        WantsAppendable plugin = (WantsAppendable) fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsAppendable");
        assertThat(plugin.out, is(instanceOf(PrintStream.class)));
        try {
            fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsAppendable");
            fail();
        } catch (CucumberException expected) {
            assertEquals("Only one plugin can use STDOUT. If you use more than one plugin you must specify output path with FORMAT:PATH_OR_URL", expected.getMessage());
        }
    }

    @Test
    public void instantiates_custom_appendable_plugin_with_stdout_and_file() throws IOException {
        WantsAppendable plugin = (WantsAppendable) fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsAppendable");
        assertThat(plugin.out, is(instanceOf(PrintStream.class)));

        WantsAppendable plugin2 = (WantsAppendable) fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsAppendable:" + TempDir.createTempFile().getAbsolutePath());
        assertEquals(UTF8OutputStreamWriter.class, plugin2.out.getClass());
    }

    @Test
    public void instantiates_custom_url_plugin() throws IOException {
        WantsUrl plugin = (WantsUrl) fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsUrl:halp");
        assertEquals(new URL("file:halp/"), plugin.out);
    }

    @Test
    public void instantiates_custom_url_plugin_with_http() throws IOException {
        WantsUrl plugin = (WantsUrl) fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsUrl:http://halp/");
        assertEquals(new URL("http://halp/"), plugin.out);
    }

    @Test
    public void instantiates_custom_uri_plugin_with_ws() throws IOException, URISyntaxException {
        WantsUri plugin = (WantsUri) fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsUri:ws://halp/");
        assertEquals(new URI("ws://halp/"), plugin.out);
    }

    @Test
    public void instantiates_custom_file_plugin() throws IOException {
        WantsFile plugin = (WantsFile) fc.create("cucumber.runtime.formatter.PluginFactoryTest$WantsFile:halp.txt");
        assertEquals(new File("halp.txt"), plugin.out);
    }

    public static class WantsAppendable extends StubFormatter {
        public final Appendable out;

        public WantsAppendable(Appendable out) {
            this.out = out;
        }
    }

    public static class WantsUrl extends StubFormatter {
        public final URL out;

        public WantsUrl(URL out) {
            this.out = out;
        }
    }

    public static class WantsUri extends StubFormatter {
        public final URI out;

        public WantsUri(URI out) {
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
