package x.micronaut

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Infrastructure
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object Chefs : IntIdTable() {
    val name = text("name")
}

class Chef(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Chef>(Chefs)

    var name by Chefs.name
}

object Locations : IntIdTable() {
    val name = text("name")
}

class Location(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Location>(Locations)

    var name by Locations.name
}

object Recipes : IntIdTable() {
    val name = text("name")
    val chef = reference("chef_id", Chefs)
}

class Recipe(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Recipe>(Recipes)

    var name by Recipes.name
    var chef by Chef referencedOn Recipes.chef
}

object Ingredients : IntIdTable() {
    val name = text("name")
    val chef = reference("chef_id", Chefs)
    val recipe = reference("recipe_id", Recipes).nullable()
}

class Ingredient(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Ingredient>(Ingredients)

    var name by Ingredients.name
    var chef by Chef referencedOn Ingredients.chef
    var recipe by Recipe optionalReferencedOn Ingredients.recipe
}

@Context
@Infrastructure
class DatabaseSetup(dataSource: DataSource) {
    private val seeSchemaInStdOut = false

    init {
        Database.connect(dataSource)
        if (seeSchemaInStdOut) transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Locations, Ingredients, Recipes, Chefs)
        }
    }
}
