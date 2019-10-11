package x.scratch;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
@Value
public final class UpsertResult<Record extends UpsertableRecord<Record>> {
    private final Record record;
    private final boolean changed;

    public static <Record extends UpsertableRecord<Record>> UpsertResult<Record> of(
            final Record entity, final Record upserted) {
        return null == upserted
                ? new UpsertResult<>(entity, false)
                : new UpsertResult<>(entity.updateWith(upserted), true);
    }
}
