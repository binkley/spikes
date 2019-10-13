package x.scratch;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

public interface UpsertableRecord<Record extends UpsertableRecord<Record>> {
    Record updateWith(Record upserted);

    @RequiredArgsConstructor(access = PRIVATE)
    @Value
    final class UpsertedRecordResult<Record extends UpsertableRecord<Record>> {
        private final Record record;
        private final boolean changed;

        public static <Record extends UpsertableRecord<Record>> UpsertedRecordResult<Record> of(
                final Record entity, final Record upserted) {
            return new UpsertedRecordResult<>(entity, null != upserted);
        }
    }
}
