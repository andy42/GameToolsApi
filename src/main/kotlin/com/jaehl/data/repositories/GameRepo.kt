package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.model.Game
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.statuspages.GameIdNotfound
import com.jaehl.statuspages.ImageIdNotfound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

interface GameRepo {
    suspend fun addNew(request : NewGameRequest) : Game?
    suspend fun getGame(gameId : Int) : Game?
    suspend fun getGames() : List<Game>
    suspend fun updateGame(id : Int, request : UpdateGameRequest) : Game?
    suspend fun deleteGame(id : Int)
}

class GameRepoImp(
    private val database: Database,
    private val coroutineScope: CoroutineScope
) : GameRepo {

    init {
        coroutineScope.launch {
            database.dbQuery {
                SchemaUtils.create(GameTable)
            }
        }
    }

    override suspend fun addNew(request : NewGameRequest) = database.dbQuery {

        val iconImage = ImageEntity.findById(request.icon) ?: throw ImageIdNotfound(request.icon)
        val bannerImage = ImageEntity.findById(request.banner) ?: throw ImageIdNotfound(request.banner)

        val gameEntity = GameEntity.new {
            this.name = request.name
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
        gameEntity.icon = iconImage.id
        gameEntity.banner = bannerImage.id
        return@dbQuery Game.create(gameEntity)
    }

    override suspend fun deleteGame(id: Int) = database.dbQuery {
        val gameEntity = GameEntity.findById(id) ?: throw  Exception("game not found : $id")
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
    var icon by GameTable.icon
    var banner by GameTable.banner
}