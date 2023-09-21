package com.jaehl.repositories

import com.jaehl.data.repositories.UserRepo
import com.jaehl.data.model.User
import com.jaehl.models.UserCredentials
import java.util.*

//password is not stored as a hash as this is only a mock
//do not use in live
class UserRepoMock : UserRepo {

    private val userMap = hashMapOf<String, User>()

    fun clear() {
        userMap.clear()
    }

    fun setUserRole(userId: String, role : User.Role) {
        val user = userMap[userId] ?: return
        userMap[userId] = user.copy(
            role = role
        )
    }

    override fun checkIfUserExists(userCredentials: UserCredentials): Boolean {
        return (userMap.values.firstOrNull { it.userName ==  userCredentials.userName} != null)
    }

    override fun createUser(userCredentials: UserCredentials) : User {
        val newId = UUID.randomUUID().toString()
        val user = User(
            id = newId,
            userName = userCredentials.userName,
            passwordHash = userCredentials.password,
            role = User.Role.User
        )
        userMap[newId] = user
        return user
    }

    override fun verifyAndGetUser(userCredentials: UserCredentials): User? {
        val user = userMap.values.firstOrNull { it.userName ==  userCredentials.userName} ?: return null
        if (user.passwordHash != userCredentials.password) return null
        return user
    }

    override fun getUser(userId: String): User? {
        return userMap[userId]
    }

    override fun getUsers(): List<User> {
        return userMap.values.toList()
    }
}