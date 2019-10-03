package x.springbootdomainpersistencemodeling

import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface ChildRepository : CrudRepository<ChildRecord, Long>

data class ChildRecord(
        @Id val id: Long?,
        val parentId: Long?,
        val naturalId: String,
        var value: String,
        val version: Int,
        val createdAt: Instant,
        val updatedAt: Instant)
