package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGameRequest(
    val name : String
)
