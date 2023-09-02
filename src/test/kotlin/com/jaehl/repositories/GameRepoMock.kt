package com.jaehl.repositories

import com.jaehl.data.model.Game
import com.jaehl.data.repositories.GameRepo
import com.jaehl.statuspages.GameIdNotfound

class GameRepoMock : GameRepo {

    private val gamesMap = hashMapOf<Int, Game>()
    private var lastIndex = 0

    fun clear() {
        gamesMap.clear()
        lastIndex = 0
    }

    override suspend fun addNew(name: String): Game? {
        val newGame = Game(
            id = lastIndex++,
            name = name
        )
        gamesMap[newGame.id] = newGame
        return newGame
    }

    override suspend fun getGame(gameId: Int): Game? {
        return gamesMap[gameId]
    }

    override suspend fun getGames(): List<Game> {
        return gamesMap.values.toList()
    }

    override suspend fun updateGame(gameId: Int, name: String): Game? {
        var updatedGame = gamesMap[gameId] ?: throw GameIdNotfound(gameId)
        updatedGame = updatedGame.copy(
            name = name
        )

        gamesMap[gameId] = updatedGame
        return updatedGame
    }

    override suspend fun deleteGame(id: Int) {
        gamesMap.remove(id)
    }
}