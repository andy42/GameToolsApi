package com.jaehl.routing

import com.jaehl.controllers.ImageController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.ImageRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.statuspages.ImageDataNotFound
import com.jaehl.statuspages.ImageIdBadRequest
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.imageRouting(imageRepo: ImageRepo, tokenManager : TokenManager, userRepo: UserRepo) {

    val imageController = ImageController(
        imageRepo = imageRepo,
        userRepo = userRepo
    )

    routing {
        authenticate("auth-jwt") {
            post("/images/new") {
                val userId = tokenManager.getUserId(call.principal<JWTPrincipal>())
                var imageId = -1
                var description = ""
                var data : ByteArray? = null

                val multipartData = call.receiveMultipart()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if(part.name == "description"){
                                description = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            data = part.streamProvider().readAllBytes()

                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if(data == null) throw ImageDataNotFound(imageId)
                data?.let { data ->
                    imageId = imageController.addNew(userId, description, data)
                }

                call.respond(
                    hashMapOf("data" to hashMapOf( "imageId" to imageId)))
            }

            get ("/images/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw ImageIdBadRequest()
                call.respondBytes(imageController.getImageData(id), ContentType.Image.PNG)
            }
        }
    }
}