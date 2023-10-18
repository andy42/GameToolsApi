package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.model.Item
import com.jaehl.data.model.ItemCategory
import com.jaehl.extensions.toItemCategory
import com.jaehl.models.requests.UpdateItemRequest
import com.jaehl.statuspages.*
import kotlinx.coroutines.CoroutineScope
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

    private fun convertItemRow(itemEntity: ItemEntity) : Item {
        return Item(
            id = itemEntity.id.value,
            name = itemEntity.name,
            categories = itemEntity.categories.map { it.toItemCategory() },
            image = itemEntity.image.value,
            game = itemEntity.game.value
        )
    }

    override suspend fun addNewItem(gameId : Int, name: String, imageId : Int, categories : List<Int>): Item = database.dbQuery {
        val image = ImageEntity.findById(imageId) ?: throw ImageIdNotfound(imageId)
        val game = GameEntity.findById(gameId) ?: throw GameIdNotfound(gameId)
        val itemEntity = ItemEntity.new {
            this.name = name
            this.image = image.id
            this.game = game.id
            this.categories = SizedCollection(
                categories.map {categoryId ->
                    CategoryEntity.findById(categoryId) ?: throw CategoryIdNotfound(categoryId)
                }
            )
        }
        return@dbQuery convertItemRow(itemEntity)
    }

    override suspend fun updateItem(itemId: Int, request: UpdateItemRequest): Item = database.dbQuery {
        val image = ImageEntity.findById(request.image) ?: throw ImageIdNotfound(request.image)
        val game = GameEntity.findById(request.game) ?: throw GameIdNotfound(request.game)
        val itemEntity = ItemEntity.findById(itemId) ?: throw NotFound("item not found $itemId")

        itemEntity.name = request.name
        itemEntity.image = image.id
        itemEntity.game = game.id
        itemEntity.categories = SizedCollection(
            request.categories.map {categoryId ->
                CategoryEntity.findById(categoryId) ?: throw CategoryIdNotfound(categoryId)
            }
        )

        return@dbQuery convertItemRow(itemEntity)
    }

    override suspend fun getItem(itemId: Int): Item = database.dbQuery {
        val itemEntity = ItemEntity.findById(itemId) ?: throw ItemIdNotfound(itemId)
        return@dbQuery convertItemRow(itemEntity)
    }

    override suspend fun getItems(): List<Item> = database.dbQuery {
        return@dbQuery ItemEntity.all().toList().map { convertItemRow(it) }
    }

    override suspend fun deleteItem(itemId: Int) = database.dbQuery {
        val itemEntity = ItemEntity.findById(itemId) ?: throw  Exception("Item not found : $itemId")
        ItemCategorieTable.deleteWhere { item eq itemId}
        itemEntity.delete()
    }

    override suspend fun addCategory(name: String): ItemCategory = database.dbQuery {
        val categoryEntity = CategoryEntity.new {
            this.name = name
        }
        return@dbQuery categoryEntity.toItemCategory()
    }

    override suspend fun getCategories(): List<ItemCategory> = database.dbQuery {
        return@dbQuery CategoryEntity.all().toList().map { it.toItemCategory() }
    }
}

object ItemTable : IntIdTable("Items") {
    val name : Column<String> = varchar("name",  50)
    val image = reference("image_id", ImageTable)
    val game = reference("game_id", GameTable)
}

class ItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ItemEntity>(ItemTable)
    var name by ItemTable.name
    var image by ItemTable.image
    var game by ItemTable.game
    var categories by CategoryEntity via ItemCategorieTable
}

object CategorieTable : IntIdTable("Categories") {
    val name = varchar("name", 100)
}

class CategoryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CategoryEntity>(CategorieTable)
    var name by CategorieTable.name
}

object ItemCategorieTable : Table("ItemCategories") {
    val item = reference("item_id", ItemTable)
    val category = reference("category_id", CategorieTable)
    override val primaryKey = PrimaryKey(item, category)
}