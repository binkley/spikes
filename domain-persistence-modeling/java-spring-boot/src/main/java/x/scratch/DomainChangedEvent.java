package x.scratch;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public abstract class DomainChangedEvent<Snapshot>
        extends ApplicationEvent {
    private final Snapshot before;
    private final Snapshot after;

    public DomainChangedEvent(final Snapshot before, final Snapshot after) {
        super(source(before, after));
        this.before = before;
        this.after = after;
    }

    public static <Snapshot, Event extends DomainChangedEvent<Snapshot>> void notifyIfChanged(
            final Snapshot before, final Snapshot after,
            final ApplicationEventPublisher publisher,
            final BiFunction<Snapshot, Snapshot, Event> event) {
        if (!Objects.equals(before, after))
            publisher.publishEvent(event.apply(before, after));
    }

    private static <Snapshot> Snapshot source(
            final Snapshot before, final Snapshot after) {
        return null == after
                ? Optional.of(before).orElseThrow()
                : after;
    }
}
