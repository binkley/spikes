package x.springbootdomainpersistencemodeling

import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface ParentRepository : CrudRepository<ParentRecord, Long>

data class ParentRecord(
        @Id val id: Long?,
        val naturalId: String,
        var value: String,
        val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant)
