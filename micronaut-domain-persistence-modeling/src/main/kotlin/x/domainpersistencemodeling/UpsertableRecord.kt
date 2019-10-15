package x.domainpersistencemodeling

interface UpsertableRecord<Record : UpsertableRecord<Record>> {
    fun updateWith(upserted: Record): Record

    data class UpsertedRecordResult<Record : UpsertableRecord<Record>>(
            val record: Record, val changed: Boolean) {
        companion object {
            fun <Record : UpsertableRecord<Record>> of(
                    entity: Record,
                    upserted: Record?): UpsertedRecordResult<Record> {
                return UpsertedRecordResult(
                        entity, null != upserted)
            }
        }
    }
}
