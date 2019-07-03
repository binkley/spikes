package x.micronaut

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

class Ingredient(private val record: IngredientRecord) {
    val name
        get() = record.name
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
