package x.scratch;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestListener<Event extends DomainChangedEvent<?>>
        implements ApplicationListener<Event> {
    private final List<Event> received = new ArrayList<>();

    @Override
    public void onApplicationEvent(@Nonnull final Event event) {
        received.add(event);
    }

    public void reset() {
        received.clear();
    }

    public List<Event> events() {
        final var events = new ArrayList<>(received);
        reset();
        return events;
    }
}
