package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserTokens(
    val refreshToken : String,
    val accessToken : String
)
