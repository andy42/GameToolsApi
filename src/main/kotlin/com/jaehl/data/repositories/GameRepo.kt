package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.model.Game
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.statuspages.CategoryIdNotfound
import com.jaehl.statuspages.GameIdNotfound
import com.jaehl.statuspages.ImageIdNotfound
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface GameRepo {
    suspend fun addNew(request : NewGameRequest) : Game
    suspend fun getGame(gameId : Int) : Game?
    suspend fun getGames() : List<Game>
    suspend fun updateGame(id : Int, request : UpdateGameRequest) : Game?
    suspend fun deleteGame(id : Int)
}

class GameRepoImp(
    private val database: Database,
    private val coroutineScope: CoroutineScope
) : GameRepo {

    override suspend fun addNew(request : NewGameRequest) = database.dbQuery {

        val iconImage = ImageEntity.findById(request.icon) ?: throw ImageIdNotfound(request.icon)
        val bannerImage = ImageEntity.findById(request.banner) ?: throw ImageIdNotfound(request.banner)

        val gameEntity = GameEntity.new {
            this.name = request.name
            this.itemCategories = SizedCollection(
                request.itemCategories.map {categoryId ->
                    CategoryEntity.findById(categoryId) ?: throw CategoryIdNotfound(categoryId)
                }
            )
            this.icon = iconImage.id
            this.banner = bannerImage.id
        }
        return@dbQuery Game.create(gameEntity)
    }

    override suspend fun getGame(gameId: Int) = database.dbQuery {
        val gameEntity = GameEntity.findById(gameId) ?: throw GameIdNotfound(gameId)
        return@dbQuery Game.create(gameEntity)
    }

    override suspend fun getGames(): List<Game> = database.dbQuery {
        return@dbQuery GameEntity.all().toList().map { Game.create(it) }
    }

    override suspend fun updateGame(id: Int, request : UpdateGameRequest) = database.dbQuery {
        val gameEntity = GameEntity.findById(id) ?: throw  Exception("game not found : $id")
        val iconImage = ImageEntity.findById(request.icon) ?: throw ImageIdNotfound(request.icon)
        val bannerImage = ImageEntity.findById(request.banner) ?: throw ImageIdNotfound(request.banner)
        gameEntity.name = request.name
        gameEntity.itemCategories = SizedCollection(
            request.itemCategories.map {categoryId ->
                CategoryEntity.findById(categoryId) ?: throw CategoryIdNotfound(categoryId)
            }
        )
        gameEntity.icon = iconImage.id
        gameEntity.banner = bannerImage.id
        return@dbQuery Game.create(gameEntity)
    }

    override suspend fun deleteGame(id: Int) = database.dbQuery {
        val gameEntity = GameEntity.findById(id) ?: throw  Exception("game not found : $id")
        GameCategoriesTable.deleteWhere { game eq  id}
        gameEntity.delete()
    }
}

object GameTable : IntIdTable("Games") {
    val name : Column<String> = varchar("name",  50)
    val icon = reference("icon", ImageTable)
    val banner = reference("banner", ImageTable)
}

class GameEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GameEntity>(GameTable)
    var name by GameTable.name
    var itemCategories by CategoryEntity via GameCategoriesTable
    var icon by GameTable.icon
    var banner by GameTable.banner
}

object GameCategoriesTable : Table("GameCategories") {
    val game = reference("game_id", GameTable)
    val category = reference("category_id", CategorieTable)
    override val primaryKey = PrimaryKey(game, category)
}