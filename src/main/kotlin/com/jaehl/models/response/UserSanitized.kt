package com.jaehl.models.response

import com.jaehl.data.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserSanitized(
    val id : Int,
    val userName : String,
    val email : String,
    val role : User.Role
)

fun User.toUserSanitized() : UserSanitized {
    return UserSanitized(
        id = this.id,
        userName = this.userName,
        email = this.userName,
        role = this.role
    )
}