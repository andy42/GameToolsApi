package com.jaehl.controllers

import com.jaehl.data.auth.TokenManager
import com.jaehl.data.model.UserToken
import com.jaehl.models.User
import com.jaehl.models.UserCredentials
import com.jaehl.data.repositories.UserRepo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class AuthController(
    private val userRepo : UserRepo,
    private val tokenManager : TokenManager
) {

    suspend fun userRegister(call: ApplicationCall) {
        val userCredentials = call.receive<UserCredentials>()

        if(userRepo.checkIfUserExists(userCredentials)){
            call.respond(HttpStatusCode.Conflict)
            return
        }
        val user = userRepo.createUser(userCredentials)

        call.respond(hashMapOf("data" to UserToken( token = tokenManager.generateJWTToken(user))))
    }

    suspend fun userLogin(call: ApplicationCall) {
        val userCredentials = call.receive<UserCredentials>()

        val user = userRepo.verifyAndGetUser(userCredentials)
        if(user == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }

        call.respond(hashMapOf("data" to UserToken( token = tokenManager.generateJWTToken(user))))
    }

    suspend fun userMe(call: ApplicationCall) {

        val principle = call.principal<JWTPrincipal>()
        val user = userRepo.getUser(tokenManager.getUserId(principle))

        if(user == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }

        call.respond(hashMapOf("data" to
                hashMapOf(
                    "id" to user.id,
                    "userName" to user.userName,
                    "role" to user.role.value
                )
        ))
    }

    suspend fun users(call: ApplicationCall) {

        val principle = call.principal<JWTPrincipal>()
        if(tokenManager.getUserRole(principle) != User.Role.Admin)  {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }
        call.respond(hashMapOf("data" to userRepo.getUsers().map {user ->
            hashMapOf(
                "id" to user.id,
                "userName" to user.userName,
                "role" to user.role.value
            )
        }))
    }
}