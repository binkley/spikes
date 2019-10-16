package x.scratch.parent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import x.scratch.UpsertableRecord.UpsertedRecordResult;
import x.scratch.child.ChildFactory;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static x.scratch.DomainChangedEvent.notifyIfChanged;
import static x.scratch.parent.ParentRecord.createRecordFor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
final class PersistedParentFactory
        implements ParentFactory {
    private final ParentRepository repository;
    private final ChildFactory children;
    private final ApplicationEventPublisher publisher;

    static ParentResource toResource(final ParentRecord record) {
        return new ParentResource(record.getNaturalId(), record.getValue(),
                record.getVersion());
    }

    @Override
    public Stream<Parent> all() {
        return stream(repository.findAll().spliterator(), false)
                .map(this::toParent);
    }

    @Override
    public Optional<Parent> findExisting(final String naturalId) {
        return repository.findByNaturalId(naturalId)
                .map(this::toParent);
    }

    @Override
    public Parent createNew(final String naturalId) {
        return new PersistedParent(this, null, createRecordFor(naturalId),
                Stream.empty());
    }

    @Override
    public Parent findExistingOrCreateNew(final String naturalId) {
        return findExisting(naturalId).orElse(createNew(naturalId));
    }

    UpsertedRecordResult<ParentRecord> save(final ParentRecord record) {
        return UpsertedRecordResult.of(record, repository.upsert(record));
    }

    void delete(final ParentRecord record) {
        repository.delete(record);
    }

    ParentRecord refresh(final String naturalId) {
        return repository.findByNaturalId(naturalId).orElseThrow();
    }

    void notifyChanged(final ParentResource before,
            final ParentResource after) {
        notifyIfChanged(before, after, publisher, ParentChangedEvent::new);
    }

    private PersistedParent toParent(final ParentRecord record) {
        final var assigned = children.findOwned(
                record.getNaturalId());
        return new PersistedParent(this, toResource(record), record,
                assigned);
    }
}
