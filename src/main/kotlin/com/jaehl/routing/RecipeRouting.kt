package com.jaehl.routing

import com.jaehl.controllers.RecipeController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.ItemRepo
import com.jaehl.data.repositories.RecipeRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.models.requests.NewRecipeRequest
import com.jaehl.models.requests.UpdateRecipeRequest
import com.jaehl.statuspages.BadRequest
import com.jaehl.statuspages.ItemBadRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.recipeRouting(recipeRepo: RecipeRepo ,itemRepo : ItemRepo, gameRepo: GameRepo, tokenManager : TokenManager, userRepo: UserRepo) {
    val recipeController = RecipeController(
        recipeRepo = recipeRepo,
        gameRepo = gameRepo,
        itemRepo = itemRepo,
        userRepo = userRepo
    )
    routing {
        authenticate("auth-jwt") {

            post("/recipes/new") {
                val userId = tokenManager.getUserId(call.principal<JWTPrincipal>())
                val newRecipeRequest = call.receive<NewRecipeRequest>()
                val response = recipeController.addRecipe(userId, newRecipeRequest)
                call.respond(hashMapOf("data" to response))
            }
            post("/recipes/{id}") {
                val userId = tokenManager.getUserId(call.principal<JWTPrincipal>())
                val recipeId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val updateRecipeRequest = call.receive<UpdateRecipeRequest>()
                val response = recipeController.updateRecipe(userId, recipeId, updateRecipeRequest)
                call.respond(hashMapOf("data" to response))
            }
            delete("/recipes/{id}") {
                val userId = tokenManager.getUserId(call.principal<JWTPrincipal>())
                val recipeId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                recipeController.deleteRecipe(userId, recipeId)
                call.respond(HttpStatusCode.OK)
            }
            get("/recipes") {
                val gameId = call.request.queryParameters["gameId"]?.let {
                    it.toIntOrNull() ?: throw ItemBadRequest("can not convert gameId to Int")
                }
                val response = recipeController.getRecipes(gameId)
                call.respond(hashMapOf("data" to response))
            }
            get("/recipes/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequest("can not convert id to Int")
                val response = recipeController.getRecipe(recipeId = id)
                call.respond(hashMapOf("data" to response))
            }
        }
    }
}