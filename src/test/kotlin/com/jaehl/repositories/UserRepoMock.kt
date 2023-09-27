package com.jaehl.repositories

import com.jaehl.data.repositories.UserRepo
import com.jaehl.data.model.User
import com.jaehl.models.UserCredentials
import com.jaehl.models.requests.UserRegisterRequest
import java.util.*

//password is not stored as a hash as this is only a mock
//do not use in live
class UserRepoMock : UserRepo {

    private val userMap = hashMapOf<Int, User>()
    private var userIndex = 0

    fun clear() {
        userMap.clear()
    }

    fun setUserRole(userId: Int, role : User.Role) {
        val user = userMap[userId] ?: return
        userMap[userId] = user.copy(
            role = role
        )
    }

    override suspend fun createUser(request: UserRegisterRequest): User {
        val newId = userIndex++
        val user = User(
            id = newId,
            userName = request.userName,
            email = request.email,
            passwordHash = request.password,
            role = User.Role.User
        )
        userMap[newId] = user
        return user
    }

    override suspend fun getUser(userId: Int): User? {
        return userMap[userId]
    }

    override suspend fun verifyAndGetUser(userCredentials: UserCredentials): User? {
        val user = userMap.values.firstOrNull { it.userName ==  userCredentials.userName} ?: return null
        if (user.passwordHash != userCredentials.password) return null
        return user
    }

    override suspend fun getUsers(): List<User> {
        return userMap.values.toList()
    }
}