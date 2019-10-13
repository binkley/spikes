package x.scratch.parent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import x.scratch.UpsertableRecord.UpsertRecordResult;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static x.scratch.DomainChangedEvent.notifyIfChanged;
import static x.scratch.parent.ParentRecord.createRecordFor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public final class PersistedParentFactory
        implements ParentFactory {
    private final ParentRepository repository;
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
        return new PersistedParent(this, null, createRecordFor(naturalId));
    }

    @Override
    public Parent findExistingOrCreateNew(final String naturalId) {
        return null;
    }

    UpsertRecordResult<ParentRecord> save(final ParentRecord record) {
        final var upserted = repository.upsert(record);
        return UpsertRecordResult.of(record, upserted);
    }

    void delete(final ParentRecord record) {
        repository.delete(record);
    }

    void notifyChanged(final ParentResource before,
            final ParentResource after) {
        notifyIfChanged(before, after, publisher, ParentChangedEvent::new);
    }

    private PersistedParent toParent(final ParentRecord record) {
        return new PersistedParent(this, toResource(record), record);
    }
}
