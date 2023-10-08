package com.jaehl.routing

import com.jaehl.controllers.CollectionController
import com.jaehl.controllers.RecipeController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.CollectionRepo
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.requests.*
import com.jaehl.statuspages.BadRequest
import com.jaehl.statuspages.ItemBadRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.collectionRouting(collectionRepo: CollectionRepo, tokenManager : TokenManager, userRepo: UserRepo) {
    val collectionController = CollectionController(
        collectionRepo = collectionRepo,
        userRepo = userRepo
    )
    routing {
        authenticate("auth-jwt") {
            post("/collections/new") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val newCollectionRequest = call.receive<NewCollectionRequest>()
                val response = collectionController.addCollection(tokenData, newCollectionRequest)
                call.respond(hashMapOf("data" to response))
            }
            post("/admin/collections/new") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val newAdminCollectionRequest = call.receive<NewAdminCollectionRequest>()
                val response = collectionController.addAdminCollection(tokenData, newAdminCollectionRequest)
                call.respond(hashMapOf("data" to response))
            }
            post("/collections/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val updateCollectionRequest = call.receive<UpdateCollectionRequest>()
                val response = collectionController.updateCollection(tokenData = tokenData, collectionId = collectionId, request = updateCollectionRequest)
                call.respond(hashMapOf("data" to response))
            }
            delete ("/collections/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                collectionController.deleteCollection(tokenData = tokenData, collectionId = collectionId)
                call.respond(HttpStatusCode.OK)
            }
            get("/collections") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val gameId = call.request.queryParameters["gameId"]?.let {
                    it.toIntOrNull() ?: throw ItemBadRequest("can not convert gameId to Int")
                }

                val response = collectionController.getCollections(tokenData = tokenData, gameId= gameId)
                call.respond(hashMapOf("data" to response))
            }
            get("/collections/{id}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val response = collectionController.getCollection(tokenData = tokenData, collectionId= collectionId)
                call.respond(hashMapOf("data" to response))
            }

            //add new Group
            post("/collections/{collectionId}/new") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["collectionId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val newCollectionGroupRequest = call.receive<NewCollectionGroupRequest>()
                val response = collectionController.addGroup(tokenData, collectionId, newCollectionGroupRequest)
                call.respond(hashMapOf("data" to response))
            }

            post("/collections/{collectionId}/{groupId}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["collectionId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val groupId = call.parameters["groupId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val updateCollectionGroupRequest = call.receive<UpdateCollectionGroupRequest>()
                val response = collectionController.updateGroup(tokenData, collectionId, groupId, updateCollectionGroupRequest)
                call.respond(hashMapOf("data" to response))
            }

            delete("/collections/{collectionId}/{groupId}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["collectionId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val groupId = call.parameters["groupId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                collectionController.deleteGroup(tokenData, collectionId, groupId)
                call.respond(HttpStatusCode.OK)
            }

            //add or update new Item amount
            post("/collections/{collectionId}/{groupId}/{itemId}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["collectionId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val groupId = call.parameters["groupId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val itemId = call.parameters["itemId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val updateCollectionItemAmountRequest = call.receive<UpdateCollectionItemAmountRequest>()
                val response = collectionController.updateItemAmount(tokenData, collectionId, groupId, itemId, updateCollectionItemAmountRequest)
                call.respond(hashMapOf("data" to response))
            }

            delete("/collections/{collectionId}/{groupId}/{itemId}") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val collectionId = call.parameters["collectionId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val groupId = call.parameters["groupId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val itemId = call.parameters["itemId"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                collectionController.deleteItemAmount(tokenData, collectionId, groupId, itemId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}