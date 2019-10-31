package x.scratch.child;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import x.scratch.UpsertableRecord.UpsertedRecordResult;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.StreamSupport.stream;
import static x.scratch.DomainChangedEvent.notifyIfChanged;
import static x.scratch.child.ChildRecord.createRecordFor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
final class PersistedChildFactory
        implements ChildFactory {
    private final ChildRepository repository;
    private final ApplicationEventPublisher publisher;

    static ChildResource toResource(final ChildRecord record) {
        return new ChildResource(record.getNaturalId(),
                record.getParentNaturalId(),
                record.getValue(), unmodifiableSet(record.getSubchildren()),
                record.getVersion());
    }

    @Override
    public Stream<Child> all() {
        return stream(repository.findAll().spliterator(), false)
                .map(this::toChild);
    }

    @Override
    public Optional<Child> findExisting(final String naturalId) {
        return repository.findByNaturalId(naturalId)
                .map(this::toChild);
    }

    @Override
    public Child createNew(final String naturalId) {
        return new PersistedChild(this, null, createRecordFor(naturalId));
    }

    @Override
    public Child findExistingOrCreateNew(final String naturalId) {
        return findExisting(naturalId).orElse(createNew(naturalId));
    }

    @Override
    public Stream<Child> findOwned(final String parentNaturalId) {
        return repository.findByParentNaturalId(parentNaturalId)
                .map(this::toChild);
    }

    UpsertedRecordResult<ChildRecord> save(final ChildRecord record) {
        final var upserted = repository.upsert(record);
        return UpsertedRecordResult.of(record, upserted);
    }

    void delete(final ChildRecord record) {
        repository.delete(record);
    }

    void notifyChanged(final ChildResource before,
            final ChildResource after) {
        notifyIfChanged(before, after, publisher, ChildChangedEvent::new);
    }

    private PersistedChild toChild(final ChildRecord record) {
        return new PersistedChild(this, toResource(record), record);
    }
}
