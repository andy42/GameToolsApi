package com.jaehl.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateItemRequest(
    val name : String,
    val game : Int,
    val categories : List<Int>,
    val image : Int
)
