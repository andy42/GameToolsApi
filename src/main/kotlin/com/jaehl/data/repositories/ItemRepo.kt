package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.model.Item
import com.jaehl.data.model.ItemCategory
import com.jaehl.models.requests.UpdateItemRequest
import com.jaehl.statuspages.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface ItemRepo {
    suspend fun addNewItem(gameId : Int, name : String, imageId : Int, categories : List<Int>) : Item
    suspend fun updateItem(itemId : Int, request : UpdateItemRequest) : Item
    suspend fun getItem(itemId : Int) : Item
    suspend fun getItems() : List<Item>
    suspend fun deleteItem(itemId : Int)

    suspend fun addCategory(name : String) : ItemCategory
    suspend fun getCategories() : List<ItemCategory>
}

class ItemRepoImp(
    private val database: Database,
    private val coroutineScope: CoroutineScope
) : ItemRepo {

    init {
        coroutineScope.launch {
            database.dbQuery {
                SchemaUtils.create(Items)
                SchemaUtils.create(Categories)
                SchemaUtils.create(ItemCategories)
            }
        }
    }

    private fun convertItemRow(itemRow: ItemRow) : Item {
        return Item(
            id = itemRow.id.value,
            name = itemRow.name,
            categories = itemRow.categories.map { convertCategoryRow(it) },
            image = itemRow.image.value,
            game = itemRow.game.value
        )
    }

    private fun convertCategoryRow(categoryRow: CategoryRow) : ItemCategory {
        return ItemCategory(
            id = categoryRow.id.value,
            name = categoryRow.name
        )
    }

    override suspend fun addNewItem(gameId : Int, name: String, imageId : Int, categories : List<Int>): Item = database.dbQuery {
        val image = ImageRow.findById(imageId) ?: throw ImageIdNotfound(imageId)
        val game = GameRow.findById(gameId) ?: throw GameIdNotfound(gameId)
        val itemRow = ItemRow.new {
            this.name = name
            this.image = image.id
            this.game = game.id
            this.categories = SizedCollection(
                categories.map {categoryId ->
                    CategoryRow.findById(categoryId) ?: throw CategoryIdNotfound(categoryId)
                }
            )
        }
        return@dbQuery convertItemRow(itemRow)
    }

    override suspend fun updateItem(itemId: Int, request: UpdateItemRequest): Item = database.dbQuery {
        val image = ImageRow.findById(request.image) ?: throw ImageIdNotfound(request.image)
        val game = GameRow.findById(request.game) ?: throw GameIdNotfound(request.game)
        val itemRow = ItemRow.findById(itemId) ?: throw NotFound("item not found $itemId")

        itemRow.name = request.name
        itemRow.image = image.id
        itemRow.game = game.id
        itemRow.categories = SizedCollection(
            request.categories.map {categoryId ->
                CategoryRow.findById(categoryId) ?: throw CategoryIdNotfound(categoryId)
            }
        )

        return@dbQuery convertItemRow(itemRow)
    }

    override suspend fun getItem(itemId: Int): Item = database.dbQuery {
        val itemRow = ItemRow.findById(itemId) ?: throw ItemIdNotfound(itemId)
        return@dbQuery convertItemRow(itemRow)
    }

    override suspend fun getItems(): List<Item> = database.dbQuery {
        return@dbQuery ItemRow.all().toList().map { convertItemRow(it) }
    }

    override suspend fun deleteItem(itemId: Int) = database.dbQuery {
        val itemRow = ItemRow.findById(itemId) ?: throw  Exception("Item not found : $itemId")
        ItemCategories.deleteWhere { item eq itemId}
        itemRow.delete()
    }

    override suspend fun addCategory(name: String): ItemCategory = database.dbQuery {
        val categoryRow = CategoryRow.new {
            this.name = name
        }
        return@dbQuery convertCategoryRow(categoryRow)
    }

    override suspend fun getCategories(): List<ItemCategory> = database.dbQuery {
        return@dbQuery CategoryRow.all().toList().map { convertCategoryRow(it) }
    }
}

object Items : IntIdTable() {
    val name : Column<String> = varchar("name",  50)
    val image = reference("image_id", Images)
    val game = reference("game_id", Games)
}

class ItemRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ItemRow>(Items)
    var name by Items.name
    var image by Items.image
    var game by Items.game
    var categories by CategoryRow via ItemCategories
}

object Categories : IntIdTable() {
    val name = varchar("name", 100)
}

class CategoryRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CategoryRow>(Categories)
    var name by Categories.name
}

object ItemCategories : Table() {
    val item = reference("item_id", Items)
    val category = reference("category_id", Categories)
    override val primaryKey = PrimaryKey(item, category)
}