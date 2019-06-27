package io.cucumber.core.eventbus;

import java.time.Instant;

import io.cucumber.core.event.Event;
import io.cucumber.core.event.EventPublisher;

public interface EventBus extends EventPublisher {

    Instant getInstant();

    void send(Event event);

    void sendAll(Iterable<Event> queue);

}
