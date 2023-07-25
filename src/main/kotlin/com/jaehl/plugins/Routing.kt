package com.jaehl.plugins

import com.jaehl.data.auth.TokenManager
import com.jaehl.repositories.UserRepo
import com.jaehl.routing.authenticationRouting
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(tokenManager : TokenManager, userRepo : UserRepo) {

    routing {
        get("/hello") {
            call.respondText("Hello World!")
        }
    }

    authenticationRouting(userRepo, tokenManager)
}
