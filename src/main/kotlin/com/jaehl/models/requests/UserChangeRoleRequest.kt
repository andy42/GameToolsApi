package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UserChangeRoleRequest(
    val userId : Int,
    val role : String
)
