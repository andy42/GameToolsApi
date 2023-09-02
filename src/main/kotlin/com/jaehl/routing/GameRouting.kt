package com.jaehl.routing

import com.jaehl.controllers.GameController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.GameRepo
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.data.repositories.UserRepo
import com.jaehl.statuspages.GameIdBadRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.gameRouting(gameRepo: GameRepo, tokenManager : TokenManager, userRepo: UserRepo) {

    val gameController = GameController(
        gameRepo = gameRepo,
        userRepo = userRepo
    )

    routing {
        authenticate("auth-jwt") {

            post("/games/new") {
                val userId = tokenManager.getUserId(call.principal<JWTPrincipal>())
                val newGameRequest = call.receive<NewGameRequest>()
                val response = gameController.addNewGame(userId, newGameRequest)
                call.respond(hashMapOf("data" to response))
            }

            post("/games/{id}") {
                val gameId = call.parameters["id"]?.toInt() ?: throw GameIdBadRequest()
                val userId = tokenManager.getUserId(call.principal<JWTPrincipal>())
                val updateGameRequest = call.receive<UpdateGameRequest>()
                val response = gameController.updateGame(userId, gameId, updateGameRequest)
                call.respond(hashMapOf("data" to response))
            }

            delete ("/games/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw GameIdBadRequest()
                val userId = tokenManager.getUserId(call.principal<JWTPrincipal>())
                gameController.deleteGame(userId, id)
                call.respond(HttpStatusCode.OK)

            }

            get("/games/{id}"){
                val id = call.parameters["id"]?.toInt() ?: throw GameIdBadRequest()
                val response = gameController.getGame(id)
                call.respond(hashMapOf("data" to response))
            }

            get("/games"){
                val response = gameController.getAllGames()
                call.respond(hashMapOf("data" to response))
            }
        }
    }
}