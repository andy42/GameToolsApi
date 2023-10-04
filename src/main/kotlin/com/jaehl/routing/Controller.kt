package com.jaehl.routing

import com.jaehl.data.auth.TokenType
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.User
import com.jaehl.data.repositories.UserRepo
import com.jaehl.statuspages.AuthorizationException

interface Controller {
    suspend fun <T>accessTokenCall(tokenData: TokenData, block : suspend () -> T ) : T {
        if(tokenData.tokenType != TokenType.AccessToken) throw AuthorizationException()
        return  block.invoke()
    }

    suspend fun <T>accessTokenCallWithRole(userRepo: UserRepo, tokenData: TokenData, allowedRoles : List<User.Role>, block : suspend (user : User) -> T ) : T {
        val user = userRepo.getUser(tokenData.userId) ?: throw AuthorizationException()
        if(!allowedRoles.contains(user.role)) throw AuthorizationException()
        if(tokenData.tokenType != TokenType.AccessToken) throw AuthorizationException()
        return  block.invoke(user)
    }
}