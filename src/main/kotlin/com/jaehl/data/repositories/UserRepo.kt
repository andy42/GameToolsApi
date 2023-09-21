package com.jaehl.data.repositories

import com.jaehl.data.auth.PasswordHashing
import com.jaehl.data.database.Database
import com.jaehl.data.local.ObjectListLoader
import com.jaehl.data.model.User
import com.jaehl.models.UserCredentials
import com.jaehl.models.requests.UserRegisterRequest
import com.jaehl.statuspages.AuthorizationException
import com.jaehl.statuspages.BadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils

interface UserRepo {
    //fun checkIfUserExists(userCredentials : UserCredentials) : Boolean
    suspend fun createUser(request : UserRegisterRequest) : User
    suspend fun verifyAndGetUser(userCredentials: UserCredentials) : User?
    suspend fun getUser(userId : Int) : User?
    suspend fun getUsers() : List<User>
}

class UserRepoImp(
    private val userListLoader : ObjectListLoader<User>,
    private val database: Database,
    private val coroutineScope: CoroutineScope,
    private val passwordHashing : PasswordHashing
) : UserRepo {

    init {
        coroutineScope.launch {

            database.dbQuery {
                SchemaUtils.create(UserTable)
            }
        }
    }

    override suspend fun createUser(request : UserRegisterRequest): User = database.dbQuery {
        return@dbQuery UserEntity.new {
            userName = request.userName
            email = request.email
            passwordHash = passwordHashing.hashPassword(request.password)
            role = User.Role.User.value
        }.toUser()
    }

    override suspend fun verifyAndGetUser(userCredentials: UserCredentials): User? = database.dbQuery {
        val user = UserEntity.find { UserTable.userName eq userCredentials.userName }.firstOrNull() ?: throw AuthorizationException()
         return@dbQuery if (!passwordHashing.verifyPassword(userCredentials.password, user.passwordHash) ) null
        else user.toUser()
    }

    override suspend fun getUser(userId: Int): User? = database.dbQuery {
        return@dbQuery UserEntity.findById(userId)?.toUser()
    }

    override suspend fun getUsers(): List<User> = database.dbQuery {
        return@dbQuery UserEntity.all().map { it.toUser() }
    }
}

object UserTable : IntIdTable() {
    val userName = varchar("userName",  50).uniqueIndex()
    val email = varchar("email",  100).uniqueIndex()
    val passwordHash = varchar("passwordHash",  100)
    val role = varchar("role", 100)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UserTable)
    var userName by UserTable.userName
    var email by UserTable.email
    var passwordHash by UserTable.passwordHash
    var role by UserTable.role

    fun toUser() : User {
        return User(
            id = this.id.value,
            userName = this.userName,
            email = this.email,
            passwordHash = this.passwordHash,
            role = User.Role.createByName(this.role) ?: throw BadRequest("role not found")
        )
    }
}