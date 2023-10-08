package com.jaehl.controllers

import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.model.User
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.data.model.Game
import com.jaehl.data.model.TokenData
import com.jaehl.data.repositories.UserRepo
import com.jaehl.routing.Controller
import com.jaehl.statuspages.AuthorizationException
import com.jaehl.statuspages.GameIdNotfound
import com.jaehl.statuspages.GameNotAddedException
import com.jaehl.statuspages.GameNotUpdatedException

class GameController(
    private val gameRepo: GameRepo,
    private val userRepo: UserRepo
) : Controller  {
    suspend fun addNewGame(tokenData : TokenData, newGameRequest : NewGameRequest) : Game =
        accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            return@accessTokenCallWithRole gameRepo.addNew(newGameRequest) ?: throw GameNotAddedException()
    }

    suspend fun updateGame(tokenData : TokenData, gameId : Int, updateGameRequest : UpdateGameRequest) : Game =
        accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()

            return@accessTokenCallWithRole gameRepo.updateGame(
                id = gameId,
                request = updateGameRequest
            ) ?: throw GameNotUpdatedException(gameId, updateGameRequest)
    }

    suspend fun deleteGame(tokenData : TokenData, gameId : Int) =
        accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            gameRepo.deleteGame(
                id = gameId
            )
    }

    suspend fun getGame(tokenData : TokenData ,gameId : Int) : Game = accessTokenCall(userRepo, tokenData){
        return@accessTokenCall gameRepo.getGame(gameId) ?: throw GameIdNotfound(gameId)
    }

    suspend fun getAllGames(tokenData : TokenData) : List<Game> = accessTokenCall(userRepo, tokenData){
        return@accessTokenCall gameRepo.getGames()
    }
}