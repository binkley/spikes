package x.scratch;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

@Getter
public abstract class DomainChangedEvent<Resource>
        extends ApplicationEvent {
    private final Resource before;
    private final Resource after;

    public DomainChangedEvent(final Resource before, final Resource after) {
        super(source(before, after));
        this.before = before;
        this.after = after;
    }

    public static <Resource, Event extends DomainChangedEvent<Resource>> void notifyIfChanged(
            final Resource before, final Resource after,
            final ApplicationEventPublisher publisher,
            final BiFunction<Resource, Resource, Event> event) {
        if (!Objects.equals(before, after))
            publisher.publishEvent(event.apply(before, after));
    }

    private static <Resource> Resource source(final Resource before,
            final Resource after) {
        return null == after
                ? Optional.of(before).orElseThrow()
                : after;
    }
}
