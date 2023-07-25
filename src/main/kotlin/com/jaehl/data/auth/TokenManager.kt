package com.jaehl.data.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.jaehl.models.User
import io.ktor.server.auth.jwt.*
import java.util.*

interface TokenManager {
    fun generateJWTToken(user : User) : String
    fun createJWTVerifier() : JWTVerifier
    fun getUserName(principle : JWTPrincipal?) : String
    fun getUserId(principle : JWTPrincipal?) : String
    fun getUserRole(principle : JWTPrincipal?) : User.Role
}

class TokenManagerImp(
    private val secret : String,
    private val issuer : String,
    private val audience : String,

    ) : TokenManager {

    override fun generateJWTToken(user : User): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim(userNameKey, user.userName)
            .withClaim(userIdKey, user.id)
            .withClaim(userRoleKey, user.role.value)
            .withExpiresAt(Date(System.currentTimeMillis() + 1000*60*24))
            .sign(Algorithm.HMAC256(secret))
    }

    override fun createJWTVerifier(): JWTVerifier {
        return JWT
            .require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    }

    override fun getUserName(principle: JWTPrincipal?): String {
        return principle?.payload?.getClaim(userNameKey)?.asString() ?: ""
    }

    override fun getUserId(principle: JWTPrincipal?): String {
        return principle?.payload?.getClaim(userIdKey)?.asString() ?: ""
    }

    override fun getUserRole(principle: JWTPrincipal?): User.Role {
        val roleString = principle?.payload?.getClaim(userRoleKey)?.asString() ?: ""
        return User.Role.createByName(roleString) ?: User.Role.User
    }

    companion object {
        val userNameKey = "username"
        val userIdKey = "userid"
        val userRoleKey = "userRole"
    }
}