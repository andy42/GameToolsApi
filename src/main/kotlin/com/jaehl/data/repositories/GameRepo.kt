package com.jaehl.data.repositories

import com.jaehl.data.database.Database
import com.jaehl.data.model.Game
import com.jaehl.statuspages.GameIdNotfound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

interface GameRepo {
    suspend fun addNew(name : String) : Game?
    suspend fun getGame(gameId : Int) : Game?
    suspend fun getGames() : List<Game>
    suspend fun updateGame(id : Int, name : String) : Game?
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

    override suspend fun addNew(name: String) = database.dbQuery {
        val gameEntity = GameEntity.new {
            this.name = name
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

    override suspend fun updateGame(id: Int, name: String) = database.dbQuery {
        val gameEntity = GameEntity.findById(id) ?: throw  Exception("game not found : $id")
        gameEntity.name = name
        return@dbQuery Game.create(gameEntity)
    }

    override suspend fun deleteGame(id: Int) = database.dbQuery {
        val gameEntity = GameEntity.findById(id) ?: throw  Exception("game not found : $id")
        gameEntity.delete()
    }
}

object GameTable : IntIdTable("Games") {
    val name : Column<String> = varchar("name",  50)
}

class GameEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GameEntity>(GameTable)
    var name by GameTable.name
}