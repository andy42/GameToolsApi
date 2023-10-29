package com.jaehl.controllers

import com.jaehl.data.auth.TokenManager
import com.jaehl.data.auth.TokenType
import com.jaehl.data.model.TokenData
import com.jaehl.data.model.UserTokens
import com.jaehl.data.model.User
import com.jaehl.models.UserCredentials
import com.jaehl.data.repositories.UserRepo
import com.jaehl.extensions.toUserSanitized
import com.jaehl.models.requests.UserChangePasswordRequest
import com.jaehl.models.requests.UserChangeRoleRequest
import com.jaehl.models.requests.UserRegisterRequest
import com.jaehl.models.response.UserSanitized
import com.jaehl.statuspages.AuthorizationException
import com.jaehl.statuspages.NotFound

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

    suspend fun userMe(tokenData : TokenData) : UserSanitized = accessTokenCall(userRepo, tokenData, allowUnverified = true) { user ->
        return@accessTokenCall user.toUserSanitized()
    }

    suspend fun getUser(tokenData : TokenData, userId : Int) : UserSanitized = accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) { _ ->
        val user = userRepo.getUser(userId) ?: throw NotFound("user not found : $userId")
        return@accessTokenCallWithRole user.toUserSanitized()
    }

    suspend fun users(tokenData : TokenData) : List<UserSanitized> = accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) {
        return@accessTokenCallWithRole userRepo.getUsers().map { it.toUserSanitized() }
    }

    suspend fun changeUserRole(tokenData : TokenData, userId : Int, request : UserChangeRoleRequest) : User = accessTokenCallWithRole(userRepo, tokenData, allowedRoles = listOf(User.Role.Admin)) {
        return@accessTokenCallWithRole userRepo.changeUserRole(userId, request)
    }

    suspend fun changeUserPassword(tokenData : TokenData, userId : Int, request : UserChangePasswordRequest) = accessTokenCall(userRepo, tokenData) { user ->
        if(user.id != userId && user.role != User.Role.Admin) throw AuthorizationException()
        userRepo.changeUserPassword(userId, request)
    }
}