package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterRequest(
    val userName : String,
    val email : String,
    val password : String
)