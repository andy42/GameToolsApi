package com.jaehl.repositories

import com.jaehl.models.User
import com.jaehl.models.UserCredentials
import java.util.*

//password is not stored as a hash as this is only a mock
//do not use in live
class UserRepoMock : UserRepo {

    private val userMap = hashMapOf<String,User>()

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
        return (userMap.values.firstOrNull { it.userName ==  userCredentials.username} != null)
    }

    override fun createUser(userCredentials: UserCredentials) : String{
        val newId = UUID.randomUUID().toString()
        userMap[newId] = User(
            id = newId,
            userName = userCredentials.username,
            passwordHash = userCredentials.password,
            role = User.Role.User
        )
        return newId
    }

    override fun verifyAndGetUser(userCredentials: UserCredentials): User? {
        val user = userMap.values.firstOrNull { it.userName ==  userCredentials.username} ?: return null
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