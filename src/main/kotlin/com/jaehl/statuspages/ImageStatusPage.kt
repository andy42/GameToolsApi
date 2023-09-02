package com.jaehl.statuspages

import com.jaehl.models.response.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.imageStatusPage(){
    exception<ImageIdNotfound> { call, cause ->
        call.respond(
            status = HttpStatusCode.NotFound,
            ErrorResponse(
                code = HttpStatusCode.NotFound.value,
                message = cause.message ?: ""
            )
        )
    }
}

class ImageIdNotfound(val imageId : Int, override val message: String? = "Image Not found $imageId") : Throwable()
class ImageIdBadRequest(override val message: String? = "Error ImageId") : Throwable()
class ImageDataNotFound(val imageId : Int, override val message: String? = "Image Data not found : $imageId") : Throwable()