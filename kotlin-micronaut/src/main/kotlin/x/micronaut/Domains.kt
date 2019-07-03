package x.micronaut

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Infrastructure
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object Locations : IntIdTable() {
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

class Location(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Location>(Locations)

    var name by Locations.name
}

object Ingredients : IntIdTable() {
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

class Ingredient(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Ingredient>(Ingredients)

    var name by Ingredients.name
}

object Recipes : IntIdTable() {
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

class Recipe(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Recipe>(Recipes)

    var name by Recipes.name
}

object Chefs : IntIdTable() {
    val name = varchar("name", length = 30) // TODO: Unlimited varchar?
}

class Chef(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Chef>(Chefs)

    var name by Chefs.name
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
