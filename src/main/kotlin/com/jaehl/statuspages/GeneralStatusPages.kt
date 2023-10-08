package com.jaehl.statuspages

import com.jaehl.models.response.ErrorResponse
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.generalStatusPages() {
    status(HttpStatusCode.Unauthorized) { call, cause ->
        call.respond(
            status = HttpStatusCode.Unauthorized,
            ErrorResponse(
                code = HttpStatusCode.Unauthorized.value,
                message = "Forbidden"
            )
        )
    }
    exception<ServerError> { call, cause ->
        call.respond(
            status = HttpStatusCode.InternalServerError,
            ErrorResponse(
                code = HttpStatusCode.InternalServerError.value,
                message = cause.message ?: ""
            )
        )
    }
    exception<BadRequest> { call, cause ->
        call.respond(
            status = HttpStatusCode.BadRequest,
            ErrorResponse(
                code = HttpStatusCode.BadRequest.value,
                message = cause.message ?: ""
            )
        )
    }
    exception<NotFound> { call, cause ->
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

class ServerError(override val message: String? = "ServerError"): Throwable()
class AuthorizationException(override val message: String? = "user not Authorized for this request") : Throwable()
class BadRequest(override val message: String? = "BadRequest") : Throwable()
class NotFound(override val message: String? = "NotFound") : Throwable()