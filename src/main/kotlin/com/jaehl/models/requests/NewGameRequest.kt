package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class NewGameRequest (
    val name : String
)