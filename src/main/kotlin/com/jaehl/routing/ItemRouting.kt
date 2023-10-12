package com.jaehl.routing

import com.jaehl.controllers.ItemController
import com.jaehl.data.auth.TokenManager
import com.jaehl.models.requests.NewCategoryRequest
import com.jaehl.models.requests.NewItemRequest
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.ItemRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.requests.UpdateItemRequest
import com.jaehl.statuspages.BadRequest
import com.jaehl.statuspages.ItemBadRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.itemRouting(itemRepo : ItemRepo, gameRepo: GameRepo, tokenManager : TokenManager, userRepo: UserRepo) {
    val itemController = ItemController(
        gameRepo = gameRepo,
        itemRepo = itemRepo,
        userRepo = userRepo
    )
    routing {
        authenticate("auth-jwt") {

            post("/items/Categories/new") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val newCategoryRequest = call.receive<NewCategoryRequest>()
                val response = itemController.addCategory(tokenData, newCategoryRequest)
                call.respond(hashMapOf("data" to response))
            }

            get("/items/Categories") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val response = itemController.getCategories(tokenData)
                call.respond(hashMapOf("data" to response))
            }
            post("/items/new") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val newItemRequest = call.receive<NewItemRequest>()
                val response = itemController.addItem(tokenData, newItemRequest)
                call.respond(hashMapOf("data" to response))
            }

            post("/items/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val itemId = call.parameters["id"]?.toIntOrNull() ?: throw ItemBadRequest("can not convert id to Int")
                val updateItemRequest = call.receive<UpdateItemRequest>()
                val response = itemController.updateItem(tokenData, itemId, updateItemRequest)
                call.respond(hashMapOf("data" to response))
            }

            get("/items") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val gameId = call.request.queryParameters["gameId"]?.let {
                    it.toIntOrNull() ?: throw ItemBadRequest("can not convert gameId to Int")
                }
                val response = itemController.getItems(tokenData, gameId)
                call.respond(hashMapOf("data" to response))
            }
            get("/items/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val id = call.parameters["id"]?.toIntOrNull() ?: throw ItemBadRequest("can not convert id to Int")
                val response = itemController.getItem(tokenData = tokenData, itemId = id)
                call.respond(hashMapOf("data" to response))
            }
            delete("/items/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val id = call.parameters["id"]?.toIntOrNull() ?: throw ItemBadRequest("can not convert id to Int")
                itemController.deleteItem(tokenData = tokenData, itemId = id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}