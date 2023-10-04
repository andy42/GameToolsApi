package com.jaehl.routing

import com.jaehl.controllers.ImageController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.ImageRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.ImageType
import com.jaehl.statuspages.BadRequest
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
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                var imageId = -1
                var description = ""
                var imageType = ImageType.NotSupported
                var data : ByteArray? = null

                val multipartData = call.receiveMultipart()
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if(part.name == "description"){
                                description = part.value
                            }
                            if(part.name == "imageType"){
                                imageType = ImageType.from(part.value.toInt())
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
                if(imageType == ImageType.NotSupported) throw BadRequest("missing imageType")
                data?.let { data ->
                    imageId = imageController.addNew(tokenData, imageType, description, data)
                }

                call.respond(
                    hashMapOf("data" to hashMapOf( "imageId" to imageId)))
            }

            get ("/images/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val id = call.parameters["id"]?.toInt() ?: throw ImageIdBadRequest()
                val imageData = imageController.getImageData(tokenData, id)
                call.respondBytes(imageData.file.readBytes(), imageTypeToContentType(imageData.imageType))
            }

            get ("/images") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val images = imageController.getImages(tokenData)
                call.respond(
                    hashMapOf("data" to images))
            }
        }
    }
}

fun imageTypeToContentType(imageType: ImageType) : ContentType{
    return when(imageType){
        ImageType.Png -> ContentType.Image.PNG
        ImageType.Webp -> ContentType("image", "webp")
        ImageType.Jpeg -> ContentType.Image.JPEG
        else -> throw Exception("unsupported image type")
    }
}