package com.jaehl.plugins

import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.ImageRepo
import com.jaehl.data.repositories.ItemRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.routing.authenticationRouting
import com.jaehl.routing.gameRouting
import com.jaehl.routing.imageRouting
import com.jaehl.routing.itemRouting
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(
    tokenManager : TokenManager,
    userRepo : UserRepo,
    gameRepo : GameRepo,
    imageRepo : ImageRepo,
    itemRepo : ItemRepo
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
}
