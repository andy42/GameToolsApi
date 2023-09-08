package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.model.Recipe
import com.jaehl.data.model.RecipeAmount
import com.jaehl.models.requests.NewRecipeRequest
import com.jaehl.models.requests.RecipeAmountRequest
import com.jaehl.models.requests.UpdateRecipeRequest
import com.jaehl.statuspages.GameIdNotfound
import com.jaehl.statuspages.ItemIdNotfound
import com.jaehl.statuspages.NotFound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere

interface RecipeRepo {
    suspend fun addRecipe(request : NewRecipeRequest) : Recipe
    suspend fun updateRecipe(recipeId : Int, request : UpdateRecipeRequest) : Recipe
    suspend fun deleteRecipe(recipeId : Int)
    suspend fun getRecipes(gameId : Int?) : List<Recipe>
    suspend fun getRecipe(recipeId : Int) : Recipe
}

class RecipeRepoImp(
    private val database: Database,
    private val coroutineScope: CoroutineScope
) : RecipeRepo {

    init {
        coroutineScope.launch {
            database.dbQuery {

//                SchemaUtils.drop(RecipeCraftedAtTable)
//                SchemaUtils.drop(RecipeInputTable)
//                SchemaUtils.drop(RecipeOutputTable)
//                SchemaUtils.drop(RecipeTable)

                SchemaUtils.create(RecipeTable)
                SchemaUtils.create(RecipeCraftedAtTable)
                SchemaUtils.create(RecipeInputTable)
                SchemaUtils.create(RecipeOutputTable)
            }
        }
    }

    private fun updateRecipeInputs(recipeEntity : RecipeEntity, inputs : List<RecipeAmountRequest>) {
        inputs.forEach { recipeAmount ->
            RecipeInputEntity.new {
                this.recipe = recipeEntity
                this.item = ItemRow.findById(recipeAmount.itemId) ?: throw ItemIdNotfound(recipeAmount.itemId)
                this.amount = recipeAmount.amount
            }
        }
    }

    private fun updateRecipeOutputs(recipeEntity : RecipeEntity, outputs : List<RecipeAmountRequest>) {
        outputs.forEach { recipeAmount ->
            RecipeOutPutEntity.new {
                this.recipe = recipeEntity
                this.item = ItemRow.findById(recipeAmount.itemId) ?: throw ItemIdNotfound(recipeAmount.itemId)
                this.amount = recipeAmount.amount
            }
        }
    }

    override suspend fun addRecipe(request : NewRecipeRequest): Recipe = database.dbQuery {

        val game = GameRow.findById(request.gameId) ?: throw NotFound("Game not found : ${request.gameId}")
        val recipeEntity = RecipeEntity.new {
            this.game = game.id
            this.craftedAt = SizedCollection(
                request.craftedAt.map { itemId ->
                    ItemRow.findById(itemId) ?: throw ItemIdNotfound(itemId)
                }
            )
        }

        updateRecipeInputs(recipeEntity, request.input)
        updateRecipeOutputs(recipeEntity, request.output)

        return@dbQuery recipeEntity.toRecipe()
    }

    override suspend fun updateRecipe(recipeId : Int, request: UpdateRecipeRequest): Recipe = database.dbQuery {
        val game = GameRow.findById(request.gameId) ?: throw NotFound("Game not found : ${request.gameId}")
        val recipeEntity = RecipeEntity.findById(recipeId) ?: throw NotFound("recipe not found : ${recipeId}")

        recipeEntity.game = game.id
        recipeEntity.craftedAt = SizedCollection(
            request.craftedAt.map { itemId ->
                ItemRow.findById(itemId) ?: throw ItemIdNotfound(itemId)
            }
        )

        RecipeInputTable.deleteWhere { recipe eq recipeId }
        updateRecipeInputs(recipeEntity, request.input)

        RecipeOutputTable.deleteWhere { recipe eq recipeId }
        updateRecipeOutputs(recipeEntity, request.output)

        return@dbQuery recipeEntity.toRecipe()
    }

    override suspend fun deleteRecipe(recipeId: Int) = database.dbQuery {
        val recipeEntity = RecipeEntity.findById(recipeId) ?: throw NotFound("recipe not found : ${recipeId}")
        recipeEntity.delete()
    }

    override suspend fun getRecipes(gameId: Int?): List<Recipe> = database.dbQuery {

        if(gameId != null){
            val game = GameRow.findById(gameId) ?: throw GameIdNotfound(gameId)
            return@dbQuery RecipeEntity.find { RecipeTable.game eq game.id }.toList().map { it.toRecipe() }
        }
        else {
            return@dbQuery RecipeEntity.all().toList().map { it.toRecipe() }
        }
    }

    override suspend fun getRecipe(recipeId: Int): Recipe = database.dbQuery {
        return@dbQuery RecipeEntity.findById(recipeId)?.toRecipe() ?: throw NotFound("recipe $recipeId not found")
    }
}

object RecipeTable : IntIdTable("recipes") {
    val game = reference("game_id", Games)
}

class RecipeEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RecipeEntity>(RecipeTable)
    var game by RecipeTable.game
    var craftedAt by ItemRow via RecipeCraftedAtTable
    val input by RecipeInputEntity referrersOn RecipeInputTable.recipe
    val output by RecipeOutPutEntity referrersOn RecipeOutputTable.recipe

    fun toRecipe() : Recipe {
        return Recipe(
            id = this.id.value,
            gameId = this.game.value,
            craftedAt = this.craftedAt.map { it.id.value },
            input = this.input.map {
                RecipeAmount(
                    id = it.id.value,
                    itemId = it.item.id.value,
                    amount = it.amount
                )
            },
            output = this.output.map {
                RecipeAmount(
                    id = it.id.value,
                    itemId = it.item.id.value,
                    amount = it.amount
                )
            }
        )
    }
}

object RecipeCraftedAtTable : Table("recipe_crafted_at") {
    val recipe = reference("recipe_id", RecipeTable)
    val item = reference("item_id", Items)
    override val primaryKey = PrimaryKey(recipe, item)
}

object RecipeInputTable : IntIdTable("recipe_inputs") {
    val recipe = reference("recipe_id", RecipeTable)
    val item = reference("item_id", Items)
    val amount = integer("amount")
}

class RecipeInputEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, RecipeInputEntity>(RecipeInputTable)
    var recipe by RecipeEntity referencedOn RecipeInputTable.recipe
    var item by ItemRow referencedOn  RecipeInputTable.item
    var amount by RecipeInputTable.amount
}

object RecipeOutputTable : IntIdTable("recipe_outputs") {
    val recipe = reference("recipe_id", RecipeTable)
    val item = reference("item_id", Items)
    val amount = integer("amount")
}

class RecipeOutPutEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, RecipeOutPutEntity>(RecipeOutputTable)
    var recipe by RecipeEntity referencedOn RecipeOutputTable.recipe
    var item by ItemRow referencedOn RecipeOutputTable.item
    var amount by RecipeOutputTable.amount
}