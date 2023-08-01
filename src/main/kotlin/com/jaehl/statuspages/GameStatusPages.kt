package com.jaehl.statuspages

import com.jaehl.models.requests.UpdateGameRequest
import com.jaehl.models.response.ErrorResponse
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.gameStatusPages() {
    exception<GameIdNotfound> { call, cause ->
        call.respond(
            status = HttpStatusCode.NotFound,
            ErrorResponse(
                code = HttpStatusCode.NotFound.value,
                message = cause.message ?: ""
            )
        )
    }
    exception<AuthorizationException> { call, cause ->
        call.respond(
            status = HttpStatusCode.Forbidden,
            ErrorResponse(
                code = HttpStatusCode.Forbidden.value,
                message = cause.message ?: ""
            )
        )
    }
}

class GameIdNotfound(val gameId : Int, override val message: String? = "game id not found for $gameId") : Throwable()
class AuthorizationException(override val message: String? = "user not Authorized for this request") : Throwable()
class GameIdBadRequest(override val message: String? = "Error GameId") : Throwable()
class GameNotAddedException(override val message: String? = "Error adding new game") : Throwable()
class GameNotUpdatedException(gameId : Int, updateGameRequest : UpdateGameRequest, override val message: String? = "Error updating game : $gameId, $updateGameRequest") : Throwable()