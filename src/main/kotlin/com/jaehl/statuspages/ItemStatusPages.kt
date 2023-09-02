package com.jaehl.statuspages

import com.jaehl.models.response.ErrorResponse
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.itemStatusPages(){
    exception<ItemIdNotfound> { call, cause ->
        call.respond(
            status = HttpStatusCode.NotFound,
            ErrorResponse(
                code = HttpStatusCode.NotFound.value,
                message = cause.message ?: ""
            )
        )
    }
    exception<ItemBadRequest> { call, cause ->
        call.respond(
            status = HttpStatusCode.BadRequest,
            ErrorResponse(
                code = HttpStatusCode.BadRequest.value,
                message = cause.message ?: ""
            )
        )
    }
    exception<CategoryIdNotfound> { call, cause ->
        call.respond(
            status = HttpStatusCode.NotFound,
            ErrorResponse(
                code = HttpStatusCode.NotFound.value,
                message = cause.message ?: ""
            )
        )
    }
}

class ItemIdNotfound(itemId : Int, override val message: String? = "item not found : $itemId") : Throwable()
class ItemBadRequest(override val message: String? = "Error ItemId") : Throwable()
class CategoryIdNotfound(categoryId : Int, override val message: String? = "item Category not found : $categoryId") : Throwable()