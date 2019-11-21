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

    static ChildSnapshot toSnapshot(final ChildRecord record) {
        return new ChildSnapshot(record.getNaturalId(),
                record.getParentNaturalId(),
                record.getValue(),
                unmodifiableSet(record.getDefaultSideValues()),
                unmodifiableSet(record.getSideValues()),
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
    public Child createNewUnassigned(final String naturalId) {
        return new PersistedChild(this, null, createRecordFor(naturalId));
    }

    @Override
    public Child findExistingOrCreateNewUnassigned(final String naturalId) {
        return findExisting(naturalId).orElse(createNewUnassigned(naturalId));
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

    void notifyChanged(final ChildSnapshot before,
            final ChildSnapshot after) {
        notifyIfChanged(before, after, publisher, ChildChangedEvent::new);
    }

    private PersistedChild toChild(final ChildRecord record) {
        return new PersistedChild(this, toSnapshot(record), record);
    }
}
