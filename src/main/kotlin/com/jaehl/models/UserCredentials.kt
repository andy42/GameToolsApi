package com.jaehl.models

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentials(
    val userName : String,
    val password : String
)
