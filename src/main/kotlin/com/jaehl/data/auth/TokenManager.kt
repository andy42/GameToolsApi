package com.jaehl.data.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.jaehl.data.model.EnvironmentConfig
import com.jaehl.data.model.User
import io.ktor.server.auth.jwt.*
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

interface TokenManager {
    fun generateJWTToken(user : User) : String
    fun createJWTVerifier() : JWTVerifier
    fun getUserName(principle : JWTPrincipal?) : String
    fun getUserId(principle : JWTPrincipal?) : Int
    fun getUserRole(principle : JWTPrincipal?) : User.Role
}

class TokenManagerImp(
    private val environmentConfig : EnvironmentConfig
    ) : TokenManager {

    override fun generateJWTToken(user : User): String {
        return JWT.create()
            .withAudience(environmentConfig.jwtAudience)
            .withIssuer(environmentConfig.jwtIssuer)
            .withClaim(userNameKey, user.userName)
            .withClaim(userIdKey, user.id)
            .withClaim(userRoleKey, user.role.value)
            .withExpiresAt(Date(System.currentTimeMillis() + 24.hours.toLong(DurationUnit.MILLISECONDS)))
            .sign(Algorithm.HMAC256(environmentConfig.jwtSecret))
    }

    override fun createJWTVerifier(): JWTVerifier {
        return JWT
            .require(Algorithm.HMAC256(environmentConfig.jwtSecret))
            .withAudience(environmentConfig.jwtAudience)
            .withIssuer(environmentConfig.jwtIssuer)
            .build()
    }

    override fun getUserName(principle: JWTPrincipal?): String {
        return principle?.payload?.getClaim(userNameKey)?.asString() ?: ""
    }

    override fun getUserId(principle: JWTPrincipal?): Int {
        return principle?.payload?.getClaim(userIdKey)?.asInt()?: -1
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