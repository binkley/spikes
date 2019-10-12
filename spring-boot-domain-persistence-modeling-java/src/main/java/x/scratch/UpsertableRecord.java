package x.scratch;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

public interface UpsertableRecord<Record extends UpsertableRecord<Record>> {
    Record updateWith(Record upserted);

    @RequiredArgsConstructor(access = PRIVATE)
    @Value
    final class UpsertRecordResult<Record extends UpsertableRecord<Record>> {
        private final Record record;
        private final boolean changed;

        public static <Record extends UpsertableRecord<Record>> UpsertRecordResult<Record> of(
                final Record entity, final Record upserted) {
            return new UpsertRecordResult<>(entity, null != upserted);
        }
    }
}
