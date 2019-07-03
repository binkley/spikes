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

object ChefRepository : IntIdTable() {
    val name = text("name")
}

class ChefRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChefRecord>(ChefRepository)

    var name by ChefRepository.name
}

object LocationRepository : IntIdTable() {
    val name = text("name")
}

class LocationRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LocationRecord>(LocationRepository)

    var name by LocationRepository.name
}

object RecipeRepository : IntIdTable() {
    val name = text("name")
    val chef = reference("chef_id", ChefRepository)
}

class RecipeRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RecipeRecord>(RecipeRepository)

    var name by RecipeRepository.name
    var chef by ChefRecord referencedOn RecipeRepository.chef
}

object IngredientRepository : IntIdTable() {
    val name = text("name")
    val chef = reference("chef_id", ChefRepository)
    val recipe = reference("recipe_id", RecipeRepository).nullable()
}

class IngredientRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<IngredientRecord>(IngredientRepository)

    var name by IngredientRepository.name
    var chef by ChefRecord referencedOn IngredientRepository.chef
    var recipe by RecipeRecord optionalReferencedOn IngredientRepository.recipe
}

@Context
@Infrastructure
class DatabaseSetup(dataSource: DataSource) {
    private val seeSchemaInStdOut = false

    init {
        Database.connect(dataSource)
        if (seeSchemaInStdOut) transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(LocationRepository, IngredientRepository, RecipeRepository, ChefRepository)
        }
    }
}
