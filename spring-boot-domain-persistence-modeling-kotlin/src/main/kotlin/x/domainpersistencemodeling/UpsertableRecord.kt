package x.domainpersistencemodeling

import java.util.Optional

interface UpsertableRecord<Record : UpsertableRecord<Record>> {
    fun upsertedWith(upserted: Record): Record

    data class UpsertedRecordResult<Record : UpsertableRecord<Record>>(
            val record: Record, val changed: Boolean) {
        companion object {
            fun <Record : UpsertableRecord<Record>> of(
                    entity: Record, upserted: Optional<Record>) =
                    UpsertedRecordResult(entity, upserted.isPresent)
        }
    }
}
