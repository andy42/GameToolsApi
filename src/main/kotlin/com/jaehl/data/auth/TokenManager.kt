package com.jaehl.data.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.jaehl.data.model.EnvironmentConfig
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.User
import io.ktor.server.auth.jwt.*
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

interface TokenManager {
    fun generateJWTToken(user : User, tokenType : TokenType) : String
    fun createJWTVerifier() : JWTVerifier
    fun getUserId(principle : JWTPrincipal?) : Int?
    fun getTokenType(principle: JWTPrincipal?): TokenType?
    fun validateForTokenType(principle: JWTPrincipal?, tokenType : TokenType) : Boolean
    fun getTokenData(principle: JWTPrincipal?) : TokenData?
}

class TokenManagerImp(
    private val environmentConfig : EnvironmentConfig
    ) : TokenManager {


    private fun generateExpireDate(tokenType : TokenType) : Long {
        return System.currentTimeMillis() + when (tokenType) {
            TokenType.AccessToken -> 5.minutes.toLong(DurationUnit.MILLISECONDS)
            TokenType.RefreshToken -> 180.days.toLong(DurationUnit.MILLISECONDS)
        }
    }

    override fun generateJWTToken(user : User, tokenType : TokenType): String {
        return JWT.create()
            .withAudience(environmentConfig.jwtAudience)
            .withIssuer(environmentConfig.jwtIssuer)
            .withClaim(userIdKey, user.id)
            .withClaim(tokenTypekey, tokenType.value)
            .withExpiresAt(Date(generateExpireDate(tokenType)))
            .sign(Algorithm.HMAC256(environmentConfig.jwtSecret))
    }

    override fun createJWTVerifier(): JWTVerifier {
        return JWT
            .require(Algorithm.HMAC256(environmentConfig.jwtSecret))
            .withAudience(environmentConfig.jwtAudience)
            .withIssuer(environmentConfig.jwtIssuer)
            .build()
    }

    override fun getUserId(principle: JWTPrincipal?): Int? {
        return principle?.payload?.getClaim(userIdKey)?.asInt()
    }

    override fun getTokenType(principle: JWTPrincipal?): TokenType? {
        return TokenType.from(principle?.payload?.getClaim(tokenTypekey)?.asString() ?: "")
    }

    override fun getTokenData(principle: JWTPrincipal?): TokenData? {

        return TokenData(
            userId = getUserId(principle) ?: return null,
            tokenType = getTokenType(principle) ?: return null
        )
    }

    override fun validateForTokenType(principle: JWTPrincipal?, tokenType: TokenType): Boolean {
        return (getTokenType(principle) == tokenType)
    }

    companion object {
        val userIdKey = "userid"
        val tokenTypekey = "tokenType"
    }
}

enum class TokenType (val value : String) {
    AccessToken("AccessToken"),
    RefreshToken("RefreshToken");

    companion object {
        fun from(value : String) : TokenType?{
            return TokenType.values().firstOrNull { it.value == value }
        }
    }
}