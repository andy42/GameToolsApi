package com.jaehl.controllers

import com.jaehl.data.auth.TokenManager
import com.jaehl.data.auth.TokenType
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.UserTokens
import com.jaehl.data.model.User
import com.jaehl.models.UserCredentials
import com.jaehl.data.repositories.UserRepo
import com.jaehl.extensions.toUserSanitized
import com.jaehl.models.requests.UserChangeRoleRequest
import com.jaehl.models.requests.UserRegisterRequest
import com.jaehl.models.response.UserSanitized
import com.jaehl.statuspages.AuthorizationException

class AuthController(
    private val userRepo : UserRepo,
    private val tokenManager : TokenManager
) : Controller {

    suspend fun userRegister(userRegisterRequest : UserRegisterRequest) : UserTokens {
        val user = userRepo.createUser(userRegisterRequest)
        return UserTokens(
            refreshToken = tokenManager.generateJWTToken(user, TokenType.RefreshToken),
            accessToken = tokenManager.generateJWTToken(user, TokenType.AccessToken)
        )
    }

    suspend fun userLogin(userCredentials : UserCredentials) : UserTokens {
        val user = userRepo.verifyAndGetUser(userCredentials) ?: throw AuthorizationException()
        return UserTokens(
            refreshToken = tokenManager.generateJWTToken(user, TokenType.RefreshToken),
            accessToken = tokenManager.generateJWTToken(user, TokenType.AccessToken)
        )
    }

    suspend fun userRefresh(tokenData : TokenData) : UserTokens {
        if(tokenData.tokenType != TokenType.RefreshToken) AuthorizationException()
        val user = userRepo.getUser(tokenData.userId) ?: throw AuthorizationException()
        if(user.userName != tokenData.userName) throw AuthorizationException()
        return UserTokens(
            refreshToken = tokenManager.generateJWTToken(user, TokenType.RefreshToken),
            accessToken = tokenManager.generateJWTToken(user, TokenType.AccessToken)
        )
    }

    suspend fun userMe(tokenData : TokenData) : UserSanitized = accessTokenCall(userRepo, tokenData) { user ->
        return@accessTokenCall user.toUserSanitized()
    }

    suspend fun users(tokenData : TokenData) : List<UserSanitized> = accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) {
        return@accessTokenCallWithRole userRepo.getUsers().map { it.toUserSanitized() }
    }

    suspend fun changeUserRole(tokenData : TokenData, request : UserChangeRoleRequest) : User = accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) {
        return@accessTokenCallWithRole userRepo.changeUserRole(request)
    }
}