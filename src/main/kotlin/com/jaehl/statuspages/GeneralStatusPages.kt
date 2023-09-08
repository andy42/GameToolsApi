package com.jaehl.statuspages

import com.jaehl.models.response.ErrorResponse
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.generalStatusPages() {
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
}

class BadRequest(override val message: String? = "BadRequest") : Throwable()
class NotFound(override val message: String? = "NotFound") : Throwable()