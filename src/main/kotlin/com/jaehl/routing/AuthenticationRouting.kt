package com.jaehl.routing

import com.jaehl.controllers.AuthController
import com.jaehl.data.auth.TokenManager
import com.jaehl.data.repositories.UserRepo
import com.jaehl.extensions.toUserSanitized
import com.jaehl.models.UserCredentials
import com.jaehl.models.requests.UserChangePasswordRequest
import com.jaehl.models.requests.UserChangeRoleRequest
import com.jaehl.models.requests.UserRegisterRequest
import com.jaehl.models.response.DataResponse
import com.jaehl.statuspages.BadRequest
import com.jaehl.statuspages.ItemBadRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.authenticationRouting(userRepo : UserRepo, tokenManager : TokenManager) {

    val authController = AuthController(userRepo, tokenManager)

    routing {
        post("/user/login") {
            val userCredentials = call.receive<UserCredentials>()
            val userToken = authController.userLogin(userCredentials)
            call.respond(DataResponse(userToken))
        }

        post("/user/register") {
            val userRegisterRequest = call.receive<UserRegisterRequest>()
            val userToken = authController.userRegister(userRegisterRequest)
            call.respond(DataResponse(userToken))
        }

        authenticate("auth-jwt") {

            post("/user/refresh") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val userToken = authController.userRefresh(tokenData)
                call.respond(DataResponse(userToken))
            }

            get("/user/me"){
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val user = authController.userMe(tokenData)
                call.respond(DataResponse(user))
            }

            get("/user/{id}"){
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val userId = call.parameters["id"]?.toIntOrNull() ?: throw ItemBadRequest("can not convert id to Int")
                val user = authController.getUser(tokenData, userId)
                call.respond(DataResponse(user))
            }

            get("/user"){
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val users = authController.users(tokenData)
                call.respond(DataResponse(users))
            }

            post("/user/{userId}/changeRole") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw ItemBadRequest("can not convert id to Int")
                val userChangeRoleRequest = call.receive<UserChangeRoleRequest>()
                val user = authController.changeUserRole(tokenData, userId, userChangeRoleRequest)
                call.respond(DataResponse(user.toUserSanitized()))
            }

            post("/user/{userId}/changePassword") {
                val jwtPrincipal = call.principal<JWTPrincipal>()
                val tokenData = tokenManager.getTokenData(jwtPrincipal) ?: throw BadRequest()
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw ItemBadRequest("can not convert id to Int")
                val userChangePasswordRequest = call.receive<UserChangePasswordRequest>()
                authController.changeUserPassword(tokenData, userId, userChangePasswordRequest)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}