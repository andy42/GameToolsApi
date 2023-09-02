package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class NewItemRequest(
    val name : String,
    val game : Int,
    val categories : List<Int>,
    val image : Int
)
