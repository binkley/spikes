package x.domainpersistencemodeling

import java.util.Optional

interface UpsertableRecord<Record : UpsertableRecord<Record>> {
    fun upsertedWith(upserted: Record): Record

    data class UpsertedRecordResult<Record : UpsertableRecord<Record>>(
            val record: Record, val changed: Boolean) {
        constructor(record: Record, upserted: Optional<Record>)
                : this(record, upserted.isPresent)
    }
}
