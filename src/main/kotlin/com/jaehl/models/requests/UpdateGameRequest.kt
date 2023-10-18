package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGameRequest(
    val name : String,
    val itemCategories : List<Int>,
    val icon : Int,
    val banner : Int
)
