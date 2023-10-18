package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class NewGameRequest (
    val name : String,
    val itemCategories : List<Int>,
    val icon : Int,
    val banner : Int
)