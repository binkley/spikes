package x.scratch;

public interface UpsertableRecord<Record extends UpsertableRecord<Record>> {
    Record updateWith(Record upserted);
}
