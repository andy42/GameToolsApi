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