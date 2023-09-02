package com.jaehl.data.repositories

import com.jaehl.data.auth.PasswordHashing
import com.jaehl.data.local.ObjectListLoader
import com.jaehl.models.User
import com.jaehl.models.UserCredentials
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

interface UserRepo {
    fun checkIfUserExists(userCredentials : UserCredentials) : Boolean
    fun createUser(userCredentials : UserCredentials) : User
    fun verifyAndGetUser(userCredentials: UserCredentials) : User?
    fun getUser(userId : String) : User?
    fun getUsers() : List<User>
}

class UserRepoImp(
    private val userListLoader : ObjectListLoader<User>,
    private val passwordHashing : PasswordHashing
) : UserRepo {

    private val users = HashMap<String, User>()

    init {
        testLoadLocal().forEach { user ->
            users[user.id] = user
        }
    }

    private fun getUserFile() : File {
        val userDirectory = Paths.get(System.getProperty("user.home"), "todoApiData")
        if( !userDirectory.exists()){
            userDirectory.createDirectory()
        }
        val userDirectoryFile = userDirectory.toFile()
        return Paths.get(userDirectoryFile.absolutePath, "users.json").toFile()
    }

    private fun testLoadLocal() : List<User> {
        val userFile = getUserFile()
        if(!userFile.exists()) return listOf()
        return userListLoader.load(userFile)
    }

    private fun getUserByUserName(userName : String) : User? {
        return users.values.firstOrNull {it.userName == userName}
    }

    override fun checkIfUserExists(userCredentials: UserCredentials): Boolean {
        return getUserByUserName(userCredentials.username) != null
    }

    private fun createNewId() : String {
        while (true){
            val newId = UUID.randomUUID().toString()
            if(!users.containsKey(newId)){
                return newId
            }
        }
    }

    override fun createUser(userCredentials : UserCredentials) : User {
        val userId = createNewId()
        val newUser = User(
            id = userId,
            userName = userCredentials.username,
            passwordHash = passwordHashing.hashPassword(userCredentials.password),
            role = User.Role.User
        )
        users[userId] = newUser
        getUserFile()
        userListLoader.save(getUserFile(), users.values.toList())
        return newUser
    }

    override fun verifyAndGetUser(userCredentials: UserCredentials) : User? {
        val user = getUserByUserName(userCredentials.username) ?: return null
        if (!passwordHashing.verifyPassword(userCredentials.password, user.passwordHash) ) return null
        return user
    }

    override fun getUser(userId: String): User? {
        return users[userId]
    }

    override fun getUsers(): List<User> {
        return users.values.toList()
    }
}