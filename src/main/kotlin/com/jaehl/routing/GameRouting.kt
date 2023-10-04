package com.jaehl.routing

import com.jaehl.controllers.GameController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.GameRepo
import com.jaehl.models.requests.NewGameRequest
import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.data.repositories.UserRepo
import com.jaehl.statuspages.BadRequest
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
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val newGameRequest = call.receive<NewGameRequest>()
                val response = gameController.addNewGame(tokenData, newGameRequest)
                call.respond(hashMapOf("data" to response))
            }

            post("/games/{id}") {
                val gameId = call.parameters["id"]?.toInt() ?: throw GameIdBadRequest()
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val updateGameRequest = call.receive<UpdateGameRequest>()
                val response = gameController.updateGame(tokenData, gameId, updateGameRequest)
                call.respond(hashMapOf("data" to response))
            }

            delete ("/games/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw GameIdBadRequest()
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                gameController.deleteGame(tokenData, id)
                call.respond(HttpStatusCode.OK)

            }

            get("/games/{id}"){
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val id = call.parameters["id"]?.toInt() ?: throw GameIdBadRequest()
                val response = gameController.getGame(tokenData, id)
                call.respond(hashMapOf("data" to response))
            }

            get("/games"){
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val response = gameController.getAllGames(tokenData)
                call.respond(hashMapOf("data" to response))
            }
        }
    }
}