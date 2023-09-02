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
                SchemaUtils.create(Games)
            }
        }
    }

    override suspend fun addNew(name: String) = database.dbQuery {
        val gameRow = GameRow.new {
            this.name = name
        }
        return@dbQuery Game.create(gameRow)
    }

    override suspend fun getGame(gameId: Int) = database.dbQuery {
        val gameRow = GameRow.findById(gameId) ?: throw GameIdNotfound(gameId)
        return@dbQuery Game.create(gameRow)
    }

    override suspend fun getGames(): List<Game> = database.dbQuery {
        return@dbQuery GameRow.all().toList().map { Game.create(it) }
    }

    override suspend fun updateGame(id: Int, name: String) = database.dbQuery {
        val gameRow = GameRow.findById(id) ?: throw  Exception("game not found : $id")
        gameRow.name = name
        return@dbQuery Game.create(gameRow)
    }

    override suspend fun deleteGame(id: Int) = database.dbQuery {
        val gameRow = GameRow.findById(id) ?: throw  Exception("game not found : $id")
        gameRow.delete()
    }
}

object Games : IntIdTable() {
    val name : Column<String> = varchar("name",  50)
}

class GameRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GameRow>(Games)
    var name by Games.name
}