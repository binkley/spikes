package x.micronaut

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Infrastructure
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object Locations : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

object Ingredients : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

object Recipes : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

object Chefs : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

@Context
@Infrastructure
class DatabaseSetup(dataSource: DataSource) {
    init {
        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(Locations, Ingredients, Recipes, Chefs)
        }
    }
}
