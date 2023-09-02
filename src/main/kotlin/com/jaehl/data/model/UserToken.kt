package com.jaehl.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserToken(
    val token : String
)
