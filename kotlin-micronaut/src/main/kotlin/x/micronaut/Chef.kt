package x.micronaut

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

class Chef(private val record: ChefRecord) {
    val name
        get() = record.name
}

object ChefRepository : IntIdTable() {
    val name = text("name")
}

class ChefRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChefRecord>(ChefRepository)

    var name by ChefRepository.name
}
