package com.jaehl.controllers

import com.jaehl.repositories.GameRepo
import com.jaehl.models.User
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.data.model.Game
import com.jaehl.repositories.UserRepo
import com.jaehl.statuspages.AuthorizationException
import com.jaehl.statuspages.GameIdNotfound
import com.jaehl.statuspages.GameNotAddedException
import com.jaehl.statuspages.GameNotUpdatedException

class GameController(
    private val gameRepo: GameRepo,
    private val userRepo: UserRepo
) {
    suspend fun addNewGame(userId : String, newGameRequest : NewGameRequest) : Game {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return gameRepo.addNew(newGameRequest.name) ?: throw GameNotAddedException()
    }

    suspend fun updateGame(userId : String, gameId : Int, updateGameRequest : UpdateGameRequest) : Game {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()

        return gameRepo.updateGame(
            id = gameId,
            name = updateGameRequest.name
        ) ?: throw GameNotUpdatedException(gameId, updateGameRequest)
    }

    suspend fun deleteGame(userId : String, gameId : Int) {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        gameRepo.deleteGame(
            id = gameId
        )
    }

    suspend fun getGame(gameId : Int) : Game {
        return gameRepo.getGame(gameId) ?: throw GameIdNotfound(gameId)
    }

    suspend fun getAllGames() : List<Game>{
        return gameRepo.getGames()
    }
}