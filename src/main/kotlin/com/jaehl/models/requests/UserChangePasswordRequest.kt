package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UserChangePasswordRequest(
    val password : String
)
