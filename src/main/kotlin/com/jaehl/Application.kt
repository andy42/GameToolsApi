package com.jaehl

import com.google.gson.reflect.TypeToken
import com.jaehl.data.auth.PasswordHashingImp
import com.jaehl.data.auth.TokenManagerImp
import com.jaehl.data.local.ObjectListJsonLoader
import com.jaehl.models.User
import io.ktor.server.application.*
import com.jaehl.plugins.*
import com.jaehl.repositories.UserRepoImp
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()

    val tokenManager = TokenManagerImp(
        secret = secret,
        issuer = issuer,
        audience = audience
    )

    val userRepo = UserRepoImp(
        userListLoader = ObjectListJsonLoader<User>(object : TypeToken<Array<User>>() {}.type),
        passwordHashing = PasswordHashingImp()
    )

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                tokenManager.createJWTVerifier()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    //configureSerialization()
    configureRouting(tokenManager, userRepo)
}
