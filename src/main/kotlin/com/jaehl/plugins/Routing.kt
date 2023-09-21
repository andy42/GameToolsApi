package com.jaehl.plugins

import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.*
import com.jaehl.routing.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(
    tokenManager : TokenManager,
    userRepo : UserRepo,
    gameRepo : GameRepo,
    imageRepo : ImageRepo,
    itemRepo : ItemRepo,
    recipeRepo : RecipeRepo,
    collectionRepo: CollectionRepo
) {

    routing {
        get("/hello") {
            call.respondText("Hello World!")
        }
    }

    authenticationRouting(userRepo, tokenManager)
    gameRouting(gameRepo, tokenManager, userRepo)
    imageRouting(imageRepo, tokenManager, userRepo)
    itemRouting(itemRepo, gameRepo, tokenManager, userRepo)
    recipeRouting(recipeRepo, itemRepo, gameRepo, tokenManager, userRepo)
    collectionRouting(collectionRepo, tokenManager, userRepo)
}
