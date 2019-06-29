package io.cucumber.core.plugin;

import io.cucumber.core.event.Event;
import io.cucumber.core.event.EventPublisher;
import io.cucumber.core.event.TestCase;
import org.apiguardian.api.API;

/**
 * Listens to pickle execution events. Can be used to
 * implement reporters.
 * <p>
 * When cucumber executes test in parallel or in a framework
 * that supports parallel execution (e.g. JUnit or TestNG)
 * {@link TestCase} events from different
 * pickles may interleave.
 * <p>
 * This interface marks an {@link EventListener} as capable of
 * understanding interleaved pickle events.
 * <p>
 * While running tests in parallel cucumber makes the
 * following guarantees:
 * <ol>
 * <li>The event publisher is synchronized. Events are not
 * published concurrently.</li>
 * <li>For test cases executed on different threads a callback
 * registered on the event publisher will be called by
 * different threads. I.e. {@code Thread.currentThread()}
 * will return a different thread for two test cases
 * executed on a different thread (but not necessarily the
 * executing thread).</li>
 * </ol>
 *
 * @see Event
 */
@API(status = API.Status.STABLE)
public interface ConcurrentEventListener extends Plugin {

    /**
     * Set the event publisher. The plugin can register event listeners with the publisher.
     *
     * @param publisher the event publisher
     */
    void setEventPublisher(EventPublisher publisher);

}
