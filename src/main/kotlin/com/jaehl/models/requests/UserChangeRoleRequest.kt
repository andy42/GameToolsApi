package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UserChangeRoleRequest(
    val role : String
)
