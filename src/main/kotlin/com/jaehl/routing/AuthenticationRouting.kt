package com.jaehl.routing

import com.jaehl.controllers.AuthController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.UserRepo
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.authenticationRouting(userRepo : UserRepo, tokenManager : TokenManager) {

    val authController = AuthController(userRepo, tokenManager)

    routing {
        post("/user/login") {
            authController.userLogin(call)
        }

        post("/user/register") {
            authController.userRegister(call)
        }

        authenticate("auth-jwt") {
            get("/user/me"){
                authController.userMe(call)
            }

            get("/user"){
                authController.users(call)
            }
        }
    }
}