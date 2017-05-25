package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.WriteEvent;
import cucumber.runner.EventBus;
import gherkin.pickles.Pickle;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.argThat;

public class ScenarioResultTest {

    private EventBus bus = mock(EventBus.class);
    private ScenarioImpl s = new ScenarioImpl(bus, mock(Pickle.class));

    @Test
    public void no_steps_is_passed() throws Exception {
        assertEquals("passed", s.getStatus());
    }

    @Test
    public void passed_failed_pending_undefined_skipped_is_failed() throws Exception {
        s.add(new Result("passed", 0L, null));
        s.add(new Result("failed", 0L, null));
        s.add(new Result("pending", 0L, null));
        s.add(new Result("undefined", 0L, null));
        s.add(new Result("skipped", 0L, null));
        assertEquals("failed", s.getStatus());
    }

    @Test
    public void passed_and_skipped_is_skipped_although_we_cant_have_skipped_without_undefined_or_pending() throws Exception {
        s.add(new Result("passed", 0L, null));
        s.add(new Result("skipped", 0L, null));
        assertEquals("skipped", s.getStatus());
    }

    @Test
    public void passed_pending_undefined_skipped_is_pending() throws Exception {
        s.add(new Result("passed", 0L, null));
        s.add(new Result("undefined", 0L, null));
        s.add(new Result("pending", 0L, null));
        s.add(new Result("skipped", 0L, null));
        assertEquals("undefined", s.getStatus());
    }

    @Test
    public void passed_undefined_skipped_is_undefined() throws Exception {
        s.add(new Result("passed", 0L, null));
        s.add(new Result("undefined", 0L, null));
        s.add(new Result("skipped", 0L, null));
        assertEquals("undefined", s.getStatus());
    }

    @Test
    public void embeds_data() {
        byte[] data = new byte[]{1, 2, 3};
        s.embed(data, "bytes/foo");
        verify(bus).send(argThat(new EmbedEventMatcher(data, "bytes/foo")));
    }

    @Test
    public void prints_output() {
        s.write("Hi");
        verify(bus).send(argThat(new WriteEventMatcher("Hi")));
    }

    @Test
    public void failed_followed_by_pending_yields_failed_error() {
        Throwable failedError = mock(Throwable.class);
        Throwable pendingError = mock(Throwable.class);

        s.add(new Result("failed", 0L, failedError));
        s.add(new Result("pending", 0L, pendingError));

        assertThat(s.getError(), sameInstance(failedError));
    }

    @Test
    public void pending_followed_by_failed_yields_failed_error() {
        Throwable pendingError = mock(Throwable.class);
        Throwable failedError = mock(Throwable.class);

        s.add(new Result("pending", 0L, pendingError));
        s.add(new Result("failed", 0L, failedError));

        assertThat(s.getError(), sameInstance(failedError));
    }
}

class EmbedEventMatcher extends ArgumentMatcher<WriteEvent> {
    private byte[] data;
    private String mimeType;

    public EmbedEventMatcher(byte[] data, String mimeType) {
        this.data = data;
        this.mimeType = mimeType;
    }

    @Override
    public boolean matches(Object argument) {
        return (argument instanceof EmbedEvent &&
                ((EmbedEvent)argument).data.equals(data) && ((EmbedEvent)argument).mimeType.equals(mimeType));
    }
}

class WriteEventMatcher extends ArgumentMatcher<WriteEvent> {
    private String text;

    public WriteEventMatcher(String text) {
        this.text = text;
    }

    @Override
    public boolean matches(Object argument) {
        return (argument instanceof WriteEvent && ((WriteEvent)argument).text.equals(text));
    }
}
