package com.jaehl.data.auth

import com.auth0.jwt.JWTVerifier
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.User
import io.ktor.server.auth.jwt.*

class TokenManagerMock : TokenManager {

    override fun generateJWTToken(user: User, tokenType: TokenType): String {
        return ""
    }

    override fun createJWTVerifier(): JWTVerifier {
        TODO("Not yet implemented")
    }

    override fun getUserId(principle: JWTPrincipal?): Int? {
        return null
    }

    override fun getTokenType(principle: JWTPrincipal?): TokenType? {
        return null
    }

    override fun getUserName(principle: JWTPrincipal?): String? {
        return null
    }

    override fun validateForTokenType(principle: JWTPrincipal?, tokenType: TokenType): Boolean {
        return false
    }

    override fun getTokenData(principle: JWTPrincipal?): TokenData? {
        return null
    }
}